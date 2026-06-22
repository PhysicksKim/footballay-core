package com.footballay.core.web.admin.local.matchcollect

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.matchcollect.FinishedMatchCollectBatchResult
import com.footballay.core.infra.matchcollect.LeagueMatchCollectManager
import com.footballay.core.infra.matchcollect.MatchCollectExecutionResult
import com.footballay.core.infra.matchcollect.MatchCollectSyncExecutor
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.FixtureMatchCollectStateRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.scheduler.MatchCollectLiveJobReconciler
import com.footballay.core.infra.scheduler.ReconcileResult
import com.footballay.core.infra.scheduler.matchjob.MatchJobKeyFactory
import com.footballay.core.logger
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
@Profile("local")
class LocalMatchCollectTestService(
    private val leagueCoreRepository: LeagueCoreRepository,
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val stateRepository: FixtureMatchCollectStateRepository,
    private val matchCollectLiveJobReconciler: MatchCollectLiveJobReconciler,
    private val leagueMatchCollectManager: LeagueMatchCollectManager,
    private val matchCollectSyncExecutor: MatchCollectSyncExecutor,
    private val scheduler: Scheduler,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val log = logger()

    @Transactional(readOnly = true)
    fun diagnostics(
        leagueUid: String?,
        fixtureUid: String?,
        includeQuartz: Boolean,
        includeState: Boolean,
    ): LocalMatchCollectDiagnosticsResponse {
        val fixture = fixtureUid?.let(fixtureCoreRepository::findNullableByUid)
        val league = resolveLeague(leagueUid, fixture)
        val recentStates =
            if (includeState && league != null) {
                stateRepository
                    .findAdminStates(
                        leagueUid = league.uid,
                        fixtureUid = fixtureUid,
                        status = null,
                        incompleteOnly = false,
                        pageable = PageRequest.of(0, 50),
                    ).content
                    .map(::toStateSnapshot)
            } else {
                emptyList()
            }

        return LocalMatchCollectDiagnosticsResponse(
            league = league?.let(::toLeagueSnapshot),
            fixture = fixture?.let(::toFixtureSnapshot),
            state = if (includeState && fixtureUid != null) stateRepository.findByFixture_Uid(fixtureUid)?.let(::toStateSnapshot) else null,
            recentStates = recentStates,
            quartzJobs = if (includeQuartz) quartzJobs(league?.uid) else emptyList(),
        )
    }

    fun reconcileLiveJobs(leagueUid: String): ReconcileResult {
        val result = matchCollectLiveJobReconciler.reconcileLeague(leagueUid)
        log.info("Local MatchCollect LIVE reconcile executed - leagueUid={}, result={}", leagueUid, result)
        return result
    }

    fun runFinishedScannerOnce(
        now: Instant?,
        batchSize: Int,
    ): FinishedMatchCollectBatchResult {
        val resolvedNow = now ?: Instant.now(clock)
        val result = leagueMatchCollectManager.collectDueFinishedFixtures(resolvedNow, batchSize.coerceIn(1, 500))
        log.info("Local MatchCollect FINISHED scanner run-once executed - now={}, result={}", resolvedNow, result)
        return result
    }

    fun collectFinished(
        fixtureUid: String,
        now: Instant?,
    ): LocalMatchCollectExecutionResponse =
        toExecutionResponse(matchCollectSyncExecutor.collectFinished(fixtureUid, now ?: Instant.now(clock)))

    fun collectPre(
        fixtureUid: String,
        now: Instant?,
    ): LocalMatchCollectExecutionResponse =
        toExecutionResponse(matchCollectSyncExecutor.collectPre(fixtureUid, now ?: Instant.now(clock)))

    fun collectLive(
        fixtureUid: String,
        now: Instant?,
    ): LocalMatchCollectExecutionResponse =
        toExecutionResponse(matchCollectSyncExecutor.collectLive(fixtureUid, now ?: Instant.now(clock)))

    fun collectPost(
        fixtureUid: String,
        now: Instant?,
    ): LocalMatchCollectExecutionResponse =
        toExecutionResponse(matchCollectSyncExecutor.collectPost(fixtureUid, now ?: Instant.now(clock)))

    fun fireQuartzJob(request: LocalQuartzFireRequest): LocalQuartzFireResponse {
        val jobKey = JobKey.jobKey(request.jobName, request.jobGroup)
        if (!scheduler.checkExists(jobKey)) {
            return LocalQuartzFireResponse(
                jobGroup = request.jobGroup,
                jobName = request.jobName,
                triggered = false,
                message = "Quartz job not found",
            )
        }

        scheduler.triggerJob(jobKey)
        log.info("Local Quartz job fired - jobKey={}", jobKey)
        return LocalQuartzFireResponse(
            jobGroup = request.jobGroup,
            jobName = request.jobName,
            triggered = true,
            message = "Quartz job trigger requested",
        )
    }

    private fun resolveLeague(
        leagueUid: String?,
        fixture: FixtureCore?,
    ): LeagueCore? =
        when {
            !leagueUid.isNullOrBlank() -> leagueCoreRepository.findByUid(leagueUid)
            fixture != null -> {
                @Suppress("DEPRECATION")
                fixture.leagueSeason?.league ?: fixture.league
            }
            else -> null
        }

    private fun quartzJobs(leagueUid: String?): List<LocalQuartzJobSnapshot> {
        val groupNames =
            scheduler.jobGroupNames
                .filter { groupName ->
                    if (leagueUid == null) {
                        groupName.startsWith("league:match:") || groupName == "batch:match-collect"
                    } else {
                        groupName == MatchJobKeyFactory.leagueMatchGroup(leagueUid)
                    }
                }

        return groupNames
            .flatMap { groupName -> scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)) }
            .sortedWith(compareBy<JobKey> { it.group }.thenBy { it.name })
            .map(::toQuartzJobSnapshot)
    }

    private fun toQuartzJobSnapshot(jobKey: JobKey): LocalQuartzJobSnapshot {
        val identity = MatchJobKeyFactory.parseJobKey(jobKey)
        val jobDetail = scheduler.getJobDetail(jobKey)
        return LocalQuartzJobSnapshot(
            jobGroup = jobKey.group,
            jobName = jobKey.name,
            jobClass = jobDetail?.jobClass?.name,
            owner = identity?.owner?.name,
            phase = identity?.phase?.name,
            leagueUid = identity?.leagueUid,
            fixtureUid = identity?.fixtureUid,
            triggers = scheduler.getTriggersOfJob(jobKey).map(::toQuartzTriggerSnapshot),
        )
    }

    private fun toQuartzTriggerSnapshot(trigger: Trigger): LocalQuartzTriggerSnapshot {
        val simpleTrigger = trigger as? SimpleTrigger
        return LocalQuartzTriggerSnapshot(
            triggerGroup = trigger.key.group,
            triggerName = trigger.key.name,
            triggerState = scheduler.getTriggerState(trigger.key).name,
            startTime = trigger.startTime?.toInstant(),
            nextFireTime = trigger.nextFireTime?.toInstant(),
            previousFireTime = trigger.previousFireTime?.toInstant(),
            repeatIntervalMillis = simpleTrigger?.repeatInterval,
            repeatCount = simpleTrigger?.repeatCount,
            timesTriggered = simpleTrigger?.timesTriggered,
        )
    }

    private fun toLeagueSnapshot(league: LeagueCore): LocalMatchCollectLeagueSnapshot =
        LocalMatchCollectLeagueSnapshot(
            leagueCoreUid = league.uid,
            name = league.name,
            available = league.available,
            matchCollect = league.matchCollect,
        )

    private fun toFixtureSnapshot(fixture: FixtureCore): LocalMatchCollectFixtureSnapshot {
        val season = fixture.leagueSeason
        @Suppress("DEPRECATION")
        val league = season?.league ?: fixture.league
        return LocalMatchCollectFixtureSnapshot(
            fixtureUid = fixture.uid,
            leagueCoreUid = league.uid,
            seasonYear = season?.seasonYear,
            currentSeason = season?.current,
            kickoff = fixture.kickoff,
            statusCode = fixture.statusCode,
            available = fixture.available,
            apiSportsFixtureId = fixture.apiSports?.apiId,
        )
    }

    private fun toStateSnapshot(state: FixtureMatchCollectState): LocalMatchCollectStateSnapshot =
        LocalMatchCollectStateSnapshot(
            fixtureUid = state.fixture.uid,
            matchCollectStatus = state.matchCollectStatus,
            lastCollectedAt = state.lastCollectedAt,
        )

    private fun toExecutionResponse(result: MatchCollectExecutionResult): LocalMatchCollectExecutionResponse =
        when (result) {
            is MatchCollectExecutionResult.Collected ->
                LocalMatchCollectExecutionResponse(
                    resultType = "COLLECTED",
                    fixtureUid = result.fixtureUid,
                    status = result.status,
                    collectedAt = result.collectedAt,
                    reason = null,
                    message = null,
                    syncResult = toSyncResultResponse(result.syncResult),
                )

            is MatchCollectExecutionResult.Skipped ->
                LocalMatchCollectExecutionResponse(
                    resultType = "SKIPPED",
                    fixtureUid = result.fixtureUid,
                    status = null,
                    collectedAt = null,
                    reason = result.reason,
                    message = null,
                    syncResult = null,
                )

            is MatchCollectExecutionResult.Failed ->
                LocalMatchCollectExecutionResponse(
                    resultType = "FAILED",
                    fixtureUid = result.fixtureUid,
                    status = null,
                    collectedAt = null,
                    reason = null,
                    message = result.message,
                    syncResult = null,
                )
        }

    private fun toSyncResultResponse(result: MatchDataSyncResult): LocalMatchDataSyncResultResponse =
        when (result) {
            is MatchDataSyncResult.PreMatch ->
                LocalMatchDataSyncResultResponse(
                    resultType = "PRE_MATCH",
                    kickoffTime = result.kickoffTime,
                    lineupCached = result.lineupCached,
                    shouldTerminatePreMatchJob = result.shouldTerminatePreMatchJob,
                )

            is MatchDataSyncResult.Live ->
                LocalMatchDataSyncResultResponse(
                    resultType = "LIVE",
                    kickoffTime = result.kickoffTime,
                    elapsedMin = result.elapsedMin,
                    statusCode = result.statusCode,
                )

            is MatchDataSyncResult.PostMatch ->
                LocalMatchDataSyncResultResponse(
                    resultType = "POST_MATCH",
                    kickoffTime = result.kickoffTime,
                    shouldStopPolling = result.shouldStopPolling,
                    minutesSinceFinish = result.minutesSinceFinish,
                )

            is MatchDataSyncResult.NotPlayed ->
                LocalMatchDataSyncResultResponse(
                    resultType = "NOT_PLAYED",
                    kickoffTime = result.kickoffTime,
                    statusCode = result.statusCode,
                )

            is MatchDataSyncResult.Error ->
                LocalMatchDataSyncResultResponse(
                    resultType = "ERROR",
                    kickoffTime = result.kickoffTime,
                    message = result.message,
                )
        }
}
