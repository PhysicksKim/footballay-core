package com.footballay.core.infra.scheduler

import com.footballay.core.logger
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
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
     * @param startTime Job 시작 시각 (킥오프 1시간 전 권장)
     * @return Job이 성공적으로 추가되었는지 여부
     */
    fun addPreMatchJob(
        fixtureUid: String,
        startTime: OffsetDateTime = OffsetDateTime.now(),
    ): Boolean {
        try {
            val jobKey = createJobKey(JOB_GROUP_PRE_MATCH, fixtureUid)

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
                    .withIdentity("pre-match-trigger-$fixtureUid", JOB_GROUP_PRE_MATCH)
                    .startAt(Date.from(startTime.toInstant()))
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInSeconds(PRE_MATCH_INTERVAL_SECONDS)
                            .withRepeatCount(PRE_MATCH_MAX_EXECUTIONS)
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
        fixtureUid: String,
        startTime: OffsetDateTime = OffsetDateTime.now(),
    ): Boolean {
        try {
            val jobKey = createJobKey(JOB_GROUP_LIVE_MATCH, fixtureUid)

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
                    .withIdentity("live-match-trigger-$fixtureUid", JOB_GROUP_LIVE_MATCH)
                    .startAt(Date.from(startTime.toInstant()))
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInSeconds(LIVE_MATCH_INTERVAL_SECONDS)
                            .withRepeatCount(LIVE_MATCH_MAX_EXECUTIONS)
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
        fixtureUid: String,
        startTime: OffsetDateTime = OffsetDateTime.now(),
    ): Boolean {
        try {
            val jobKey = createJobKey(JOB_GROUP_POST_MATCH, fixtureUid)

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
                    .withIdentity("post-match-trigger-$fixtureUid", JOB_GROUP_POST_MATCH)
                    .startAt(Date.from(startTime.toInstant()))
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInSeconds(POST_MATCH_INTERVAL_SECONDS)
                            .withRepeatCount(POST_MATCH_MAX_EXECUTIONS)
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

        val preMatchKey = createJobKey(JOB_GROUP_PRE_MATCH, fixtureUid)
        val liveMatchKey = createJobKey(JOB_GROUP_LIVE_MATCH, fixtureUid)
        val postMatchKey = createJobKey(JOB_GROUP_POST_MATCH, fixtureUid)

        if (removeJob(preMatchKey)) deletedCount++
        if (removeJob(liveMatchKey)) deletedCount++
        if (removeJob(postMatchKey)) deletedCount++

        log.info("Removed all jobs for fixtureUid={}, count={}", fixtureUid, deletedCount)
        return deletedCount
    }

    /**
     * Job 존재 확인
     *
     * @param jobKey 확인할 Job의 Key
     * @return Job이 존재하는지 여부
     */
    fun jobExists(jobKey: JobKey): Boolean {
        return try {
            scheduler.checkExists(jobKey)
        } catch (e: Exception) {
            log.error("Failed to check job existence - jobKey={}", jobKey, e)
            false
        }
    }

    /**
     * JobKey 생성 헬퍼
     */
    private fun createJobKey(
        groupName: String,
        fixtureUid: String,
    ): JobKey = JobKey.jobKey("$groupName-$fixtureUid", groupName)

    companion object {
        // Job Group 이름
        private const val JOB_GROUP_PRE_MATCH = "pre-match"
        private const val JOB_GROUP_LIVE_MATCH = "live-match"
        private const val JOB_GROUP_POST_MATCH = "post-match"

        // PreMatch Job 설정 (60초 간격, 최대 5시간 = 300회)
        private const val PRE_MATCH_INTERVAL_SECONDS = 60
        private const val PRE_MATCH_MAX_EXECUTIONS = 300

        // LiveMatch Job 설정 (17초 간격, 최대 5시간 = 1058회)
        private const val LIVE_MATCH_INTERVAL_SECONDS = 17
        private const val LIVE_MATCH_MAX_EXECUTIONS = 1058

        // PostMatch Job 설정 (60초 간격, 최대 1시간 = 60회)
        private const val POST_MATCH_INTERVAL_SECONDS = 60
        private const val POST_MATCH_MAX_EXECUTIONS = 60
    }
}

