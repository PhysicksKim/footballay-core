package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.MatchDataSyncDispatcher
import com.footballay.core.logger
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import java.time.OffsetDateTime

/**
 * 경기 전 라인업 캐싱 Job
 *
 * 경기 시작 전 1시간 전부터 라인업 발표를 체크하며 polling합니다.
 * 라인업이 발표되면 저장하고, 킥오프가 임박하면 LiveMatchJob으로 전환합니다.
 *
 * **동작 방식:**
 * 1. JobDataMap에서 fixtureUid 추출
 * 2. Dispatcher에게 동기화 요청
 */
class PreMatchJob(
    private val dispatcher: MatchDataSyncDispatcher,
    private val transitionManager: AvailableFixtureJobTransitionManager,
    private val cacheRefreshPublisher: AvailableFixtureCacheRefreshPublisher,
) : Job {
    private val log = logger()

    override fun execute(context: JobExecutionContext) {
        val fixtureUid = context.mergedJobDataMap.getString(KEY_FIXTURE_UID)
        val executionTime = OffsetDateTime.now()

        if (fixtureUid.isNullOrBlank()) {
            log.error("PreMatchJob: fixtureUid is null or blank in JobDataMap")
            throw JobExecutionException("fixtureUid is required")
        }

        log.info("PreMatchJob executing - fixtureUid={}, time={}", fixtureUid, executionTime)

        try {
            val result = dispatcher.syncByFixtureUid(fixtureUid)
            val jobContext =
                AvailableFixtureJobContext(
                    phase = AvailableFixtureJobPhase.PRE_MATCH,
                    jobKey = context.jobDetail.key,
                )
            transitionManager.handle(fixtureUid, result, jobContext)
            cacheRefreshPublisher.publishIfNeeded(fixtureUid, result, AvailableFixtureJobPhase.PRE_MATCH)
            log.info("PreMatchJob completed - fixtureUid={}, result={}", fixtureUid, result)
        } catch (e: Exception) {
            log.error("PreMatchJob execution failed - fixtureUid={}", fixtureUid, e)
            throw JobExecutionException("PreMatchJob failed for fixtureUid=$fixtureUid", e)
        }
    }

    companion object {
        const val KEY_FIXTURE_UID = "fixtureUid"
    }
}
