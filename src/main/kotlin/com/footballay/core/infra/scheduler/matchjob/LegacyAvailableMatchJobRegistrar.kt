package com.footballay.core.infra.scheduler.matchjob

import com.footballay.core.infra.scheduler.LiveMatchJob
import com.footballay.core.infra.scheduler.PostMatchJob
import com.footballay.core.infra.scheduler.PreMatchJob
import com.footballay.core.logger
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

/**
 * 이전 버전 네이밍 및 형식으로 등록된 Job을 처리하기 위한 전용 클래스입니다.
 */
@Service
class LegacyAvailableMatchJobRegistrar(
    private val scheduler: Scheduler,
    private val matchJobRegistrar: MatchJobRegistrar,
) {
    private val log = logger()

    @Deprecated("Use addPreMatchJob(leagueUid, fixtureUid, startTime)")
    fun addPreMatchJob(
        fixtureUid: String,
        startTime: Instant,
    ): Boolean =
        addLegacyAvailableJob(
            fixtureUid = fixtureUid,
            startTime = startTime,
            groupName = LEGACY_PRE_MATCH_GROUP,
            jobClass = PreMatchJob::class.java,
            intervalSeconds = LEGACY_PRE_MATCH_INTERVAL_SECONDS,
            repeatCount = LEGACY_PRE_MATCH_MAX_EXECUTIONS,
            jobLabel = "PreMatchJob",
        )

    @Deprecated("Use addLiveMatchJob(leagueUid, fixtureUid, startTime)")
    fun addLiveMatchJob(
        fixtureUid: String,
        startTime: Instant,
    ): Boolean =
        addLegacyAvailableJob(
            fixtureUid = fixtureUid,
            startTime = startTime,
            groupName = LEGACY_LIVE_MATCH_GROUP,
            jobClass = LiveMatchJob::class.java,
            intervalSeconds = LEGACY_LIVE_MATCH_INTERVAL_SECONDS,
            repeatCount = LEGACY_LIVE_MATCH_MAX_EXECUTIONS,
            jobLabel = "LiveMatchJob",
        )

    @Deprecated("Use addPostMatchJob(leagueUid, fixtureUid, startTime)")
    fun addPostMatchJob(
        fixtureUid: String,
        startTime: Instant,
    ): Boolean =
        addLegacyAvailableJob(
            fixtureUid = fixtureUid,
            startTime = startTime,
            groupName = LEGACY_POST_MATCH_GROUP,
            jobClass = PostMatchJob::class.java,
            intervalSeconds = LEGACY_POST_MATCH_INTERVAL_SECONDS,
            repeatCount = LEGACY_POST_MATCH_MAX_EXECUTIONS,
            jobLabel = "PostMatchJob",
        )

    fun deleteLegacyAvailableFixtureJobs(fixtureUid: String): Int {
        var deletedCount = 0

        val preMatchKey = MatchJobKeyFactory.legacyAvailableJobKey(LEGACY_PRE_MATCH_GROUP, fixtureUid)
        val liveMatchKey = MatchJobKeyFactory.legacyAvailableJobKey(LEGACY_LIVE_MATCH_GROUP, fixtureUid)
        val postMatchKey = MatchJobKeyFactory.legacyAvailableJobKey(LEGACY_POST_MATCH_GROUP, fixtureUid)

        if (matchJobRegistrar.removeJob(preMatchKey)) deletedCount++
        if (matchJobRegistrar.removeJob(liveMatchKey)) deletedCount++
        if (matchJobRegistrar.removeJob(postMatchKey)) deletedCount++

        log.info("Removed all legacy available jobs for fixtureUid={}, count={}", fixtureUid, deletedCount)
        return deletedCount
    }

    private fun addLegacyAvailableJob(
        fixtureUid: String,
        startTime: Instant,
        groupName: String,
        jobClass: Class<out org.quartz.Job>,
        intervalSeconds: Int,
        repeatCount: Int,
        jobLabel: String,
    ): Boolean {
        try {
            log.info("Adding legacy {} fixtures for {}", jobLabel, fixtureUid)
            val jobKey = MatchJobKeyFactory.legacyAvailableJobKey(groupName, fixtureUid)

            if (scheduler.checkExists(jobKey)) {
                log.warn("{} already exists for fixtureUid={}, removing first", jobLabel, fixtureUid)
                matchJobRegistrar.removeJob(jobKey)
            }

            val job =
                JobBuilder
                    .newJob(jobClass)
                    .withIdentity(jobKey)
                    .usingJobData(MatchJobDataKeys.KEY_FIXTURE_UID, fixtureUid)
                    .build()

            val trigger =
                TriggerBuilder
                    .newTrigger()
                    .withIdentity("$groupName-trigger-$fixtureUid", groupName)
                    .startAt(Date.from(startTime))
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInSeconds(intervalSeconds)
                            .withRepeatCount(repeatCount)
                            .withMisfireHandlingInstructionNowWithRemainingCount(),
                    ).build()

            scheduler.scheduleJob(job, trigger)
            log.info("{} added - fixtureUid={}, startTime={}", jobLabel, fixtureUid, startTime)
            return true
        } catch (e: Exception) {
            log.error("Failed to add {} for fixtureUid={}", jobLabel, fixtureUid, e)
            return false
        }
    }

    companion object {
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
