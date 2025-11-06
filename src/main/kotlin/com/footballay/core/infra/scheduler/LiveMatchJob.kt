package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.JobContext
import com.footballay.core.infra.dispatcher.match.MatchDataSyncDispatcher
import com.footballay.core.logger
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import java.time.OffsetDateTime

/**
 * 경기 진행 중 라이브 데이터 동기화 Job
 *
 * 경기가 시작되면 17초 간격으로 실시간 데이터를 polling합니다.
 * 이벤트, 스코어, 선수 스탯 등을 실시간으로 동기화합니다.
 * 경기가 종료되면 PostMatchJob으로 전환됩니다.
 *
 * **동작 방식:**
 * 1. JobContext에서 fixtureUid 추출
 * 2. Dispatcher에게 동기화 요청
 * 3. Result.Live.isMatchFinished가 true면 Dispatcher가 PostMatchJob으로 전환
 *
 * **주의사항:**
 * - Match data sync는 1.5~3초 소요 → Worker Thread 점유 주의
 * - 향후 비동기/코루틴으로 전환 예정
 */
class LiveMatchJob(
    private val dispatcher: MatchDataSyncDispatcher,
) : Job {
    private val log = logger()

    override fun execute(context: JobExecutionContext) {
        val fixtureUid = context.mergedJobDataMap.getString(KEY_FIXTURE_UID)
        val executionTime = OffsetDateTime.now()

        if (fixtureUid.isNullOrBlank()) {
            log.error("LiveMatchJob: fixtureUid is null or blank in JobDataMap")
            throw JobExecutionException("fixtureUid is required")
        }

        log.info("LiveMatchJob executing - fixtureUid={}, time={}", fixtureUid, executionTime)

        try {
            val jobContext = JobContext.liveMatch(context.jobDetail.key)
            val result = dispatcher.syncByFixtureUid(fixtureUid, jobContext)
            log.info("LiveMatchJob completed - fixtureUid={}, result={}", fixtureUid, result)
        } catch (e: Exception) {
            log.error("LiveMatchJob execution failed - fixtureUid={}", fixtureUid, e)
            throw JobExecutionException("LiveMatchJob failed for fixtureUid=$fixtureUid", e)
        }
    }

    companion object {
        const val KEY_FIXTURE_UID = "fixtureUid"
    }
}
