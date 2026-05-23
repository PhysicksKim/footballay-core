package com.footballay.core.infra.scheduler

import com.footballay.core.logger
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleTrigger
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

/**
 * Quartz Job 스케줄링 관리 서비스
 *
 * Match Sync Job의 생명주기를 관리합니다.
 * - PreMatchJob: 경기 전 라인업 캐싱 (60초 간격)
 * - LiveMatchJob: 경기 중 실시간 동기화 (17초 간격)
 * - PostMatchJob: 경기 후 최종 데이터 확정 (60초 간격)
 *
 * **Job 전환 흐름:**
 * Available Fixture 등록 → PreMatchJob → LiveMatchJob → PostMatchJob → Job 삭제
 *
 * **책임:**
 * - Job 추가/삭제/조회
 * - Job 전환 시 이전 Job 삭제
 * - Job 실행 간격 및 최대 실행 시간 관리
 */
@Service
class JobSchedulerService(
    private val scheduler: Scheduler,
) {
    private val log = logger()

    /**
     * PreMatchJob 추가
     *
     * 경기 시작 전 라인업 캐싱을 위한 Job을 등록합니다.
     *
     * @param fixtureUid Fixture UID
     * @param startTime Job 시작 시각 (Instant, 킥오프 1시간 전 권장)
     * @return Job이 성공적으로 추가되었는지 여부
     */
    fun addPreMatchJob(
        leagueUid: String,
        fixtureUid: String,
        startTime: Instant,
    ): Boolean =
        registerOrReplaceAvailableJob(
            phase = MatchJobPhase.PRE,
            leagueUid = leagueUid,
            fixtureUid = fixtureUid,
            startTime = startTime,
        ).success

    @Deprecated("Use addPreMatchJob(leagueUid, fixtureUid, startTime)")
    fun addPreMatchJob(
        fixtureUid: String,
        startTime: Instant,
    ): Boolean {
        try {
            log.info("Adding PreMatchJob fixtures for $fixtureUid")
            val jobKey = MatchJobKeyFactory.legacyAvailableJobKey(LEGACY_PRE_MATCH_GROUP, fixtureUid)

            // 이미 존재하면 삭제
            if (scheduler.checkExists(jobKey)) {
                log.warn("PreMatchJob already exists for fixtureUid={}, removing first", fixtureUid)
                removeJob(jobKey)
            }

            val job =
                JobBuilder
                    .newJob(PreMatchJob::class.java)
                    .withIdentity(jobKey)
                    .usingJobData(PreMatchJob.KEY_FIXTURE_UID, fixtureUid)
                    .build()

            val trigger =
                TriggerBuilder
                    .newTrigger()
                    .withIdentity("pre-match-trigger-$fixtureUid", LEGACY_PRE_MATCH_GROUP)
                    .startAt(Date.from(startTime))
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInSeconds(LEGACY_PRE_MATCH_INTERVAL_SECONDS)
                            .withRepeatCount(LEGACY_PRE_MATCH_MAX_EXECUTIONS)
                            .withMisfireHandlingInstructionNowWithRemainingCount(),
                    ).build()

            scheduler.scheduleJob(job, trigger)
            log.info("PreMatchJob added - fixtureUid={}, startTime={}", fixtureUid, startTime)
            return true
        } catch (e: Exception) {
            log.error("Failed to add PreMatchJob for fixtureUid={}", fixtureUid, e)
            return false
        }
    }

    /**
     * LiveMatchJob 추가
     *
     * 경기 진행 중 실시간 데이터 동기화를 위한 Job을 등록합니다.
     *
     * @param fixtureUid Fixture UID
     * @param startTime Job 시작 시각 (킥오프 시각 권장)
     * @return Job이 성공적으로 추가되었는지 여부
     */
    fun addLiveMatchJob(
        leagueUid: String,
        fixtureUid: String,
        startTime: Instant,
    ): Boolean =
        registerOrReplaceAvailableJob(
            phase = MatchJobPhase.LIVE,
            leagueUid = leagueUid,
            fixtureUid = fixtureUid,
            startTime = startTime,
        ).success

    @Deprecated("Use addLiveMatchJob(leagueUid, fixtureUid, startTime)")
    fun addLiveMatchJob(
        fixtureUid: String,
        startTime: Instant,
    ): Boolean {
        try {
            log.info("Adding LiveMatchJob fixtures for $fixtureUid")
            val jobKey = MatchJobKeyFactory.legacyAvailableJobKey(LEGACY_LIVE_MATCH_GROUP, fixtureUid)

            // 이미 존재하면 삭제
            if (scheduler.checkExists(jobKey)) {
                log.warn("LiveMatchJob already exists for fixtureUid={}, removing first", fixtureUid)
                removeJob(jobKey)
            }

            val job =
                JobBuilder
                    .newJob(LiveMatchJob::class.java)
                    .withIdentity(jobKey)
                    .usingJobData(LiveMatchJob.KEY_FIXTURE_UID, fixtureUid)
                    .build()

            val trigger =
                TriggerBuilder
                    .newTrigger()
                    .withIdentity("live-match-trigger-$fixtureUid", LEGACY_LIVE_MATCH_GROUP)
                    .startAt(Date.from(startTime))
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInSeconds(LEGACY_LIVE_MATCH_INTERVAL_SECONDS)
                            .withRepeatCount(LEGACY_LIVE_MATCH_MAX_EXECUTIONS)
                            .withMisfireHandlingInstructionNowWithRemainingCount(),
                    ).build()

            scheduler.scheduleJob(job, trigger)
            log.info("LiveMatchJob added - fixtureUid={}, startTime={}", fixtureUid, startTime)
            return true
        } catch (e: Exception) {
            log.error("Failed to add LiveMatchJob for fixtureUid={}", fixtureUid, e)
            return false
        }
    }

    /**
     * PostMatchJob 추가
     *
     * 경기 종료 후 최종 데이터 확정을 위한 Job을 등록합니다.
     *
     * @param fixtureUid Fixture UID
     * @param startTime Job 시작 시각 (경기 종료 직후 권장)
     * @return Job이 성공적으로 추가되었는지 여부
     */
    fun addPostMatchJob(
        leagueUid: String,
        fixtureUid: String,
        startTime: Instant = Instant.now(),
    ): Boolean =
        registerOrReplaceAvailableJob(
            phase = MatchJobPhase.POST,
            leagueUid = leagueUid,
            fixtureUid = fixtureUid,
            startTime = startTime,
        ).success

    @Deprecated("Use addPostMatchJob(leagueUid, fixtureUid, startTime)")
    fun addPostMatchJob(
        fixtureUid: String,
        startTime: Instant = Instant.now(),
    ): Boolean {
        try {
            log.info("Adding PostMatchJob fixtures for $fixtureUid")
            val jobKey = MatchJobKeyFactory.legacyAvailableJobKey(LEGACY_POST_MATCH_GROUP, fixtureUid)

            // 이미 존재하면 삭제
            if (scheduler.checkExists(jobKey)) {
                log.warn("PostMatchJob already exists for fixtureUid={}, removing first", fixtureUid)
                removeJob(jobKey)
            }

            val job =
                JobBuilder
                    .newJob(PostMatchJob::class.java)
                    .withIdentity(jobKey)
                    .usingJobData(PostMatchJob.KEY_FIXTURE_UID, fixtureUid)
                    .build()

            val trigger =
                TriggerBuilder
                    .newTrigger()
                    .withIdentity("post-match-trigger-$fixtureUid", LEGACY_POST_MATCH_GROUP)
                    .startAt(Date.from(startTime))
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInSeconds(LEGACY_POST_MATCH_INTERVAL_SECONDS)
                            .withRepeatCount(LEGACY_POST_MATCH_MAX_EXECUTIONS)
                            .withMisfireHandlingInstructionNowWithRemainingCount(),
                    ).build()

            scheduler.scheduleJob(job, trigger)
            log.info("PostMatchJob added - fixtureUid={}, startTime={}", fixtureUid, startTime)
            return true
        } catch (e: Exception) {
            log.error("Failed to add PostMatchJob for fixtureUid={}", fixtureUid, e)
            return false
        }
    }

    /**
     * Job 삭제
     *
     * @param jobKey 삭제할 Job의 Key
     * @return Job이 성공적으로 삭제되었는지 여부
     */
    fun removeJob(jobKey: JobKey): Boolean {
        try {
            log.info("Removing Job fixtures for $jobKey")
            val deleted = scheduler.deleteJob(jobKey)
            if (deleted) {
                log.info("Job removed - jobKey={}", jobKey)
            } else {
                log.warn("Job not found for removal - jobKey={}", jobKey)
            }
            return deleted
        } catch (e: Exception) {
            log.error("Failed to remove job - jobKey={}", jobKey, e)
            return false
        }
    }

    /**
     * Fixture와 관련된 모든 Job 삭제 (Pre/Live/Post)
     *
     * Available Fixture를 해제할 때 사용합니다.
     *
     * @param fixtureUid Fixture UID
     * @return 삭제된 Job 수
     */
    fun removeAllJobsForFixture(fixtureUid: String): Int {
        var deletedCount = 0

        val preMatchKey = MatchJobKeyFactory.legacyAvailableJobKey(LEGACY_PRE_MATCH_GROUP, fixtureUid)
        val liveMatchKey = MatchJobKeyFactory.legacyAvailableJobKey(LEGACY_LIVE_MATCH_GROUP, fixtureUid)
        val postMatchKey = MatchJobKeyFactory.legacyAvailableJobKey(LEGACY_POST_MATCH_GROUP, fixtureUid)

        if (removeJob(preMatchKey)) deletedCount++
        if (removeJob(liveMatchKey)) deletedCount++
        if (removeJob(postMatchKey)) deletedCount++

        log.info("Removed all jobs for fixtureUid={}, count={}", fixtureUid, deletedCount)
        return deletedCount
    }

    fun deleteFixtureJobs(
        leagueUid: String,
        fixtureUid: String,
        owner: MatchJobOwner? = null,
    ): Int {
        val owners = owner?.let(::listOf) ?: MatchJobOwner.entries
        var deletedCount = 0

        owners.forEach { currentOwner ->
            MatchJobPhase.entries.forEach { phase ->
                val identity =
                    MatchJobIdentity(
                        owner = currentOwner,
                        phase = phase,
                        leagueUid = leagueUid,
                        fixtureUid = fixtureUid,
                    )
                if (delete(identity)) {
                    deletedCount++
                }
            }
        }

        log.info("Deleted fixture match jobs - leagueUid={}, fixtureUid={}, owner={}, count={}", leagueUid, fixtureUid, owner, deletedCount)
        return deletedCount
    }

    /**
     * Job 존재 확인
     *
     * @param jobKey 확인할 Job의 Key
     * @return Job이 존재하는지 여부
     */
    fun jobExists(jobKey: JobKey): Boolean =
        try {
            scheduler.checkExists(jobKey)
        } catch (e: Exception) {
            log.error("Failed to check job existence - jobKey={}", jobKey, e)
            false
        }

    fun registerOrReplace(
        identity: MatchJobIdentity,
        schedule: MatchJobSchedule,
    ): Boolean = registerOrReplaceDetailed(identity, schedule).success

    fun registerOrReplaceAvailableJob(
        phase: MatchJobPhase,
        leagueUid: String,
        fixtureUid: String,
        startTime: Instant,
        compareStartAt: Boolean = true,
    ): MatchJobRegistrationResult =
        registerOrReplaceDetailed(
            identity =
                MatchJobIdentity(
                    owner = MatchJobOwner.AVAILABLE,
                    phase = phase,
                    leagueUid = leagueUid,
                    fixtureUid = fixtureUid,
                ),
            schedule =
                AvailableMatchJobSchedulePolicy.schedule(
                    phase = phase,
                    startAt = startTime,
                    compareStartAt = compareStartAt,
                ),
        )

    fun registerOrReplaceDetailed(
        identity: MatchJobIdentity,
        schedule: MatchJobSchedule,
    ): MatchJobRegistrationResult {
        val jobKey = MatchJobKeyFactory.jobKey(identity)
        try {
            if (scheduler.checkExists(jobKey)) {
                if (matchesDesiredSpec(jobKey, schedule)) {
                    log.info("Match job already matches desired spec - jobKey={}", jobKey)
                    return MatchJobRegistrationResult.Unchanged
                }

                log.info("Match job spec changed - replacing jobKey={}", jobKey)
                if (!removeJob(jobKey)) {
                    return MatchJobRegistrationResult.Failed("Failed to remove existing job: $jobKey")
                }

                register(identity, schedule)
                return MatchJobRegistrationResult.Replaced
            }

            register(identity, schedule)
            return MatchJobRegistrationResult.Registered
        } catch (e: Exception) {
            log.error("Failed to register match job - identity={}", identity, e)
            return MatchJobRegistrationResult.Failed(e.message ?: e::class.simpleName.orEmpty())
        }
    }

    fun delete(identity: MatchJobIdentity): Boolean = removeJob(MatchJobKeyFactory.jobKey(identity))

    fun listLeagueMatchJobs(leagueUid: String): Set<JobKey> =
        try {
            findJobKeysOf(MatchJobKeyFactory.leagueMatchGroup(leagueUid))
        } catch (e: Exception) {
            log.error("Failed to list league match jobs - leagueUid={}", leagueUid, e)
            emptySet()
        }

    fun deleteStartupAvailableMatchJobs(): MatchJobCleanupResult {
        val accumulator = MatchJobCleanupAccumulator()

        MatchJobKeyFactory.legacyAvailableJobGroups.forEach { groupName ->
            deleteJobsInGroup(
                groupName = groupName,
                scope = "legacy-available-group",
                accumulator = accumulator,
            )
        }

        val currentGroups =
            try {
                scheduler.getJobGroupNames().filter(MatchJobKeyFactory::isLeagueMatchGroup)
            } catch (e: Exception) {
                accumulator.addCurrentGroupError(e)
                emptyList()
            }

        currentGroups.forEach { groupName ->
            deleteJobsInGroup(
                groupName = groupName,
                scope = "current-available-owner",
                accumulator = accumulator,
            ) { jobKey ->
                MatchJobKeyFactory.parseJobKey(jobKey)?.owner == MatchJobOwner.AVAILABLE
            }
        }

        val result = accumulator.toResult()
        log.info(
            "Startup available match job cleanup finished - deleted={}, skipped={}, success={}, errors={}",
            result.deleted,
            result.skipped,
            result.success,
            result.errors.size,
        )
        return result
    }

    /**
     * Match Job이 일치하는지 검사
     */
    private fun matchesDesiredSpec(
        jobKey: JobKey,
        schedule: MatchJobSchedule,
    ): Boolean {
        val jobDetail = scheduler.getJobDetail(jobKey) ?: return false
        if (jobDetail.jobClass != schedule.jobClass) {
            return false
        }

        val trigger = scheduler.getTriggersOfJob(jobKey).singleOrNull() as? SimpleTrigger ?: return false

        return (!schedule.compareStartAt || trigger.startTime == Date.from(schedule.startAt)) &&
            trigger.repeatInterval == schedule.repeatIntervalMillis &&
            trigger.repeatCount == schedule.repeatCount
    }

    private fun register(
        identity: MatchJobIdentity,
        schedule: MatchJobSchedule,
    ) {
        val jobKey = MatchJobKeyFactory.jobKey(identity)
        val triggerKey = MatchJobKeyFactory.triggerKey(identity)
        val job =
            JobBuilder
                .newJob(schedule.jobClass)
                .withIdentity(jobKey)
                .usingJobData(KEY_FIXTURE_UID, identity.fixtureUid)
                .build()

        val trigger =
            TriggerBuilder
                .newTrigger()
                .withIdentity(triggerKey)
                .forJob(jobKey)
                .startAt(Date.from(schedule.startAt))
                .withSchedule(
                    SimpleScheduleBuilder
                        .simpleSchedule()
                        .withIntervalInSeconds(schedule.repeatIntervalSeconds)
                        .withRepeatCount(schedule.repeatCount)
                        .withMisfireHandlingInstructionNowWithRemainingCount(),
                ).build()

        scheduler.scheduleJob(job, trigger)
        log.info("Registered match job - jobKey={}, triggerKey={}", jobKey, triggerKey)
    }

    private fun deleteJobsInGroup(
        groupName: String,
        scope: String,
        accumulator: MatchJobCleanupAccumulator,
        shouldDelete: (JobKey) -> Boolean = { true },
    ) {
        val jobKeys =
            try {
                findJobKeysOf(groupName)
            } catch (e: Exception) {
                val operation = "list-jobs:$scope"
                val message = e.message ?: e::class.simpleName.orEmpty()
                accumulator.addCleanupError(groupName, operation, message)
                return
            }

        jobKeys.forEach { jobKey ->
            if (!shouldDelete(jobKey)) {
                accumulator.skipped++
                return@forEach
            }

            try {
                if (scheduler.deleteJob(jobKey)) {
                    accumulator.deleted++
                } else {
                    val operation = "delete:$scope"
                    val message = "Quartz returned false while deleting job"
                    accumulator.addCleanupError(groupName, operation, message, jobKey)
                }
            } catch (e: Exception) {
                val operation = "delete:$scope"
                val message = e.message ?: e::class.simpleName.orEmpty()
                accumulator.addCleanupError(groupName, operation, message, jobKey)
            }
        }
    }

    private fun findJobKeysOf(groupName: String): Set<JobKey> = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))

    companion object {
        const val KEY_FIXTURE_UID = "fixtureUid"

        private const val LEGACY_PRE_MATCH_GROUP = "pre-match"
        private const val LEGACY_LIVE_MATCH_GROUP = "live-match"
        private const val LEGACY_POST_MATCH_GROUP = "post-match"

        private const val LEGACY_PRE_MATCH_INTERVAL_SECONDS = 60
        private const val LEGACY_PRE_MATCH_MAX_EXECUTIONS = 300
        private const val LEGACY_LIVE_MATCH_INTERVAL_SECONDS = 17
        private const val LEGACY_LIVE_MATCH_MAX_EXECUTIONS = 1058
        private const val LEGACY_POST_MATCH_INTERVAL_SECONDS = 60
        private const val LEGACY_POST_MATCH_MAX_EXECUTIONS = 60
    }
}

data class MatchJobCleanupError(
    val groupName: String?,
    val jobKey: JobKey?,
    val operation: String,
    val message: String,
)

data class MatchJobCleanupResult(
    val deleted: Int,
    val skipped: Int,
    val errors: List<MatchJobCleanupError>,
) {
    val success: Boolean
        get() = errors.isEmpty()
}

private data class MatchJobCleanupAccumulator(
    var deleted: Int = 0,
    var skipped: Int = 0,
    var errors: List<MatchJobCleanupError> = emptyList(),
) {
    fun toResult(): MatchJobCleanupResult =
        MatchJobCleanupResult(
            deleted = deleted,
            skipped = skipped,
            errors = errors,
        )

    fun addCurrentGroupError(e: Exception) {
        this.errors +=
            MatchJobCleanupError(
                groupName = null,
                jobKey = null,
                operation = "list-current-groups",
                message = e.message ?: e::class.simpleName.orEmpty(),
            )
    }

    fun addCleanupError(
        groupName: String,
        operation: String,
        message: String,
        jobKey: JobKey? = null,
    ) {
        this.errors +=
            MatchJobCleanupError(
                groupName = groupName,
                jobKey = jobKey,
                operation = operation,
                message = message,
            )
    }
}

sealed class MatchJobRegistrationResult {
    abstract val success: Boolean

    data object Registered : MatchJobRegistrationResult() {
        override val success: Boolean = true
    }

    data object Replaced : MatchJobRegistrationResult() {
        override val success: Boolean = true
    }

    data object Unchanged : MatchJobRegistrationResult() {
        override val success: Boolean = true
    }

    data class Failed(
        val message: String,
    ) : MatchJobRegistrationResult() {
        override val success: Boolean = false
    }
}
