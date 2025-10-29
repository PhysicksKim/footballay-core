package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.JobContext
import com.footballay.core.infra.dispatcher.match.MatchDataSyncDispatcher
import com.footballay.core.logger
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import java.time.OffsetDateTime

/**
 * 경기 종료 후 최종 데이터 동기화 Job
 *
 * 경기가 종료된 후 60초 간격으로 최종 데이터를 확정합니다.
 * 일정 시간(60분) 후에는 더 이상 변경이 없으므로 polling을 중단합니다.
 *
 * **동작 방식:**
 * 1. JobContext에서 fixtureUid 추출
 * 2. Dispatcher에게 동기화 요청
 * 3. Result.PostMatch.shouldStopPolling이 true면 Dispatcher가 Job 삭제
 *
 * **중단 조건:**
 * - 경기 종료 후 60분 경과
 * - 최종 데이터가 확정되면 더 이상 변경이 없음
 */
class PostMatchJob(
    private val dispatcher: MatchDataSyncDispatcher,
) : Job {
    private val log = logger()

    override fun execute(context: JobExecutionContext) {
        val fixtureUid = context.mergedJobDataMap.getString(KEY_FIXTURE_UID)
        val executionTime = OffsetDateTime.now()

        if (fixtureUid.isNullOrBlank()) {
            log.error("PostMatchJob: fixtureUid is null or blank in JobDataMap")
            throw JobExecutionException("fixtureUid is required")
        }

        log.info("PostMatchJob executing - fixtureUid={}, time={}", fixtureUid, executionTime)

        try {
            val jobContext = JobContext.postMatch(context.jobDetail.key)
            val result = dispatcher.syncByFixtureUid(fixtureUid, jobContext)
            log.info("PostMatchJob completed - fixtureUid={}, result={}", fixtureUid, result)
        } catch (e: Exception) {
            log.error("PostMatchJob execution failed - fixtureUid={}", fixtureUid, e)
            throw JobExecutionException("PostMatchJob failed for fixtureUid=$fixtureUid", e)
        }
    }

    companion object {
        const val KEY_FIXTURE_UID = "fixtureUid"
    }
}

