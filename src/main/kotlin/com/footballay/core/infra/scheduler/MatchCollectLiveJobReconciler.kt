package com.footballay.core.infra.scheduler

import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.match.FixtureStatusClassifier
import com.footballay.core.infra.match.FixtureStatusGroup
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.FixtureMatchCollectStateRepository
import com.footballay.core.infra.scheduler.matchjob.MatchJobIdentity
import com.footballay.core.infra.scheduler.matchjob.MatchJobKeyFactory
import com.footballay.core.infra.scheduler.matchjob.MatchJobOwner
import com.footballay.core.infra.scheduler.matchjob.MatchJobPhase
import com.footballay.core.infra.scheduler.matchjob.MatchJobRegistrationResult
import com.footballay.core.logger
import org.quartz.JobKey
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration
import java.time.Instant

interface MatchCollectLiveJobReconciler {
    fun reconcileLeague(leagueUid: String): ReconcileResult
}

@Component
class MatchCollectLiveJobReconcilerImpl(
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val stateRepository: FixtureMatchCollectStateRepository,
    private val jobSchedulerService: JobSchedulerService,
    private val fixtureStatusClassifier: FixtureStatusClassifier,
    private val clock: Clock = Clock.systemUTC(),
) : MatchCollectLiveJobReconciler {
    private val log = logger()

    @Transactional
    override fun reconcileLeague(leagueUid: String): ReconcileResult {
        val fixtures = fixtureCoreRepository.findMatchCollectLiveJobReconcileFixturesByLeagueUid(leagueUid)
        if (fixtures.isEmpty()) {
            return ReconcileResult.empty(fixtureUid = null, leagueUid = leagueUid)
        }

        return combineResults(
            fixtureUid = null,
            leagueUid = leagueUid,
            results = fixtures.map(::reconcileFixture),
        )
    }

    fun reconcileFixture(fixture: FixtureCore): ReconcileResult {
        val league = fixture.leagueSeason?.league ?: fixture.league
        val leagueUid = league.uid
        val desired = desiredJobs(fixture, Instant.now(clock))
        val accumulator =
            ReconcileAccumulator(
                fixtureUid = fixture.uid,
                leagueUid = leagueUid,
                planned = desired.size,
            )

        if (desired.isNotEmpty()) {
            deleteOwnerJobsIfPresent(fixture.uid, leagueUid, MatchJobOwner.AVAILABLE, accumulator)
        }

        MatchJobPhase.entries.forEach { phase ->
            val desiredJob = desired[phase]
            if (desiredJob == null) {
                deleteActualIfPresent(fixture.uid, leagueUid, phase, accumulator)
            } else {
                applyDesired(fixture.uid, leagueUid, desiredJob, accumulator)
            }
        }

        return accumulator.toResult()
    }

    private fun desiredJobs(
        fixture: FixtureCore,
        now: Instant,
    ): Map<MatchJobPhase, DesiredMatchCollectJob> {
        val leagueSeason = fixture.leagueSeason ?: return emptyMap()
        val league = leagueSeason.league
        val kickoff = fixture.kickoff
        if (!league.available || league.matchCollect != MatchCollect.LIVE || !leagueSeason.current || fixture.available || kickoff == null) {
            return emptyMap()
        }

        val statusGroup = fixtureStatusClassifier.groupOf(fixture.statusCode)
        if (statusGroup == FixtureStatusGroup.UNKNOWN) {
            log.warn("MatchCollect LIVE fixture has unknown status - fixtureUid={}, status={}", fixture.uid, fixture.statusCode)
            return emptyMap()
        }
        if (statusGroup == FixtureStatusGroup.NOT_PLAYED) {
            markNotPlayed(fixture)
            return emptyMap()
        }

        val liveWindowEnd = kickoff.plus(LIVE_COLLECTION_WINDOW)
        val matchWindowEnd = liveWindowEnd.plus(POST_COLLECTION_WINDOW)

        return when {
            now.isBefore(kickoff) -> {
                if (kickoff.isAfter(now.plus(MATCH_COLLECT_LIVE_LOOKAHEAD_WINDOW))) {
                    emptyMap()
                } else {
                    mapOf(
                        MatchJobPhase.PRE to
                            DesiredMatchCollectJob(
                                phase = MatchJobPhase.PRE,
                                startAt = kickoff.minus(PRE_COLLECTION_LEAD_TIME),
                                compareStartAt = true,
                            ),
                        MatchJobPhase.LIVE to
                            DesiredMatchCollectJob(
                                phase = MatchJobPhase.LIVE,
                                startAt = kickoff,
                                compareStartAt = true,
                            ),
                    )
                }
            }

            now.isBefore(liveWindowEnd) -> {
                when (statusGroup) {
                    FixtureStatusGroup.PENDING,
                    FixtureStatusGroup.LIVE,
                    -> {
                        mapOf(MatchJobPhase.LIVE to DesiredMatchCollectJob(MatchJobPhase.LIVE, kickoff, true))
                    }

                    FixtureStatusGroup.NORMAL_FINISHED -> {
                        mapOf(MatchJobPhase.POST to DesiredMatchCollectJob(MatchJobPhase.POST, now, false))
                    }

                    FixtureStatusGroup.NOT_PLAYED,
                    FixtureStatusGroup.UNKNOWN,
                    -> {
                        emptyMap()
                    }
                }
            }

            now.isBefore(matchWindowEnd) -> {
                if (statusGroup == FixtureStatusGroup.NORMAL_FINISHED) {
                    mapOf(MatchJobPhase.POST to DesiredMatchCollectJob(MatchJobPhase.POST, now, false))
                } else {
                    emptyMap()
                }
            }

            else -> emptyMap()
        }
    }

    private fun markNotPlayed(fixture: FixtureCore) {
        val state = fixture.matchCollectState
        if (state == null) {
            val saved =
                stateRepository.save(
                    FixtureMatchCollectState(
                        fixture = fixture,
                        matchCollectStatus = MatchCollectStatus.NOT_PLAYED,
                    ),
                )
            fixture.matchCollectState = saved
        } else if (state.matchCollectStatus != MatchCollectStatus.NOT_PLAYED) {
            state.matchCollectStatus = MatchCollectStatus.NOT_PLAYED
        }
    }

    private fun applyDesired(
        fixtureUid: String,
        leagueUid: String,
        desired: DesiredMatchCollectJob,
        accumulator: ReconcileAccumulator,
    ) {
        when (
            val result =
                jobSchedulerService.registerOrReplaceMatchCollectJob(
                    phase = desired.phase,
                    leagueUid = leagueUid,
                    fixtureUid = fixtureUid,
                    startTime = desired.startAt,
                    compareStartAt = desired.compareStartAt,
                )
        ) {
            MatchJobRegistrationResult.Registered -> accumulator.registered++
            MatchJobRegistrationResult.Replaced -> accumulator.replaced++
            MatchJobRegistrationResult.Unchanged -> accumulator.skipped++
            is MatchJobRegistrationResult.Failed -> {
                accumulator.errors +=
                    ReconcileError(
                        fixtureUid = fixtureUid,
                        leagueUid = leagueUid,
                        phase = desired.phase,
                        operation = "register-or-replace",
                        message = result.message,
                    )
            }
        }
    }

    private fun deleteActualIfPresent(
        fixtureUid: String,
        leagueUid: String,
        phase: MatchJobPhase,
        accumulator: ReconcileAccumulator,
    ) {
        val jobKey = matchCollectJobKey(leagueUid, fixtureUid, phase)
        if (!jobSchedulerService.jobExists(jobKey)) {
            return
        }

        if (jobSchedulerService.removeJob(jobKey)) {
            accumulator.deleted++
        } else {
            accumulator.errors +=
                ReconcileError(
                    fixtureUid = fixtureUid,
                    leagueUid = leagueUid,
                    phase = phase,
                    operation = "delete",
                    message = "Failed to delete existing match collect job: $jobKey",
                )
        }
    }

    private fun deleteOwnerJobsIfPresent(
        fixtureUid: String,
        leagueUid: String,
        owner: MatchJobOwner,
        accumulator: ReconcileAccumulator,
    ) {
        MatchJobPhase.entries.forEach { phase ->
            val identity =
                MatchJobIdentity(
                    owner = owner,
                    phase = phase,
                    leagueUid = leagueUid,
                    fixtureUid = fixtureUid,
                )
            val jobKey = MatchJobKeyFactory.jobKey(identity)
            if (!jobSchedulerService.jobExists(jobKey)) {
                return@forEach
            }
            if (jobSchedulerService.removeJob(jobKey)) {
                accumulator.deleted++
            } else {
                accumulator.errors +=
                    ReconcileError(
                        fixtureUid = fixtureUid,
                        leagueUid = leagueUid,
                        phase = phase,
                        operation = "delete-${owner.key}",
                        message = "Failed to delete existing ${owner.key} job: $jobKey",
                    )
            }
        }
    }

    private fun matchCollectJobKey(
        leagueUid: String,
        fixtureUid: String,
        phase: MatchJobPhase,
    ): JobKey {
        val identity =
            MatchJobIdentity(
                owner = MatchJobOwner.MATCHCOLLECT,
                phase = phase,
                leagueUid = leagueUid,
                fixtureUid = fixtureUid,
            )
        return MatchJobKeyFactory.jobKey(identity)
    }

    private fun combineResults(
        fixtureUid: String?,
        leagueUid: String?,
        results: List<ReconcileResult>,
    ): ReconcileResult =
        ReconcileResult(
            fixtureUid = fixtureUid,
            leagueUid = leagueUid,
            success = results.all { it.success },
            planned = results.sumOf { it.planned },
            registered = results.sumOf { it.registered },
            replaced = results.sumOf { it.replaced },
            deleted = results.sumOf { it.deleted },
            skipped = results.sumOf { it.skipped },
            errors = results.flatMap { it.errors },
        )

    private data class DesiredMatchCollectJob(
        val phase: MatchJobPhase,
        val startAt: Instant,
        val compareStartAt: Boolean,
    )

    private data class ReconcileAccumulator(
        val fixtureUid: String,
        val leagueUid: String,
        val planned: Int,
        var registered: Int = 0,
        var replaced: Int = 0,
        var deleted: Int = 0,
        var skipped: Int = 0,
        var errors: List<ReconcileError> = emptyList(),
    ) {
        fun toResult(): ReconcileResult =
            ReconcileResult(
                fixtureUid = fixtureUid,
                leagueUid = leagueUid,
                success = errors.isEmpty(),
                planned = planned,
                registered = registered,
                replaced = replaced,
                deleted = deleted,
                skipped = skipped,
                errors = errors,
            )
    }

    companion object {
        val MATCH_COLLECT_LIVE_LOOKAHEAD_WINDOW: Duration = Duration.ofHours(72)
        val PRE_COLLECTION_LEAD_TIME: Duration = Duration.ofHours(1)
        val LIVE_COLLECTION_WINDOW: Duration = Duration.ofHours(4)
        val POST_COLLECTION_WINDOW: Duration = Duration.ofHours(1)
    }
}
