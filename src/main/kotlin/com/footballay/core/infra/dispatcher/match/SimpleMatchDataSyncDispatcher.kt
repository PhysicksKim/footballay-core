package com.footballay.core.infra.dispatcher.match

import com.footballay.core.infra.match.MatchSyncOrchestrator
import com.footballay.core.infra.scheduler.JobSchedulerService
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * 매치 데이터 동기화 Dispatcher 기본 구현체
 *
 * 등록된 MatchSyncOrchestrator 구현체들을 순회하며,
 * 해당 fixture를 지원하는 Provider를 찾아 동기화를 수행합니다.
 *
 * **동작 방식:**
 * 1. 모든 Orchestrator에 대해 `isSupport(fixtureUid)` 호출
 * 2. 지원하는 Orchestrator를 찾으면 `syncMatchData(fixtureUid)` 호출
 * 3. 동기화 Result에 따라 Job 전환 결정 (JobContext가 있는 경우만)
 * 4. 지원하는 Orchestrator가 없으면 fallback 결과 반환
 *
 * **Job 전환 로직:**
 * - PreMatch.readyForLive = true → LiveMatchJob 전환
 * - Live.isMatchFinished = true → PostMatchJob 전환
 * - PostMatch.shouldStopPolling = true → Job 삭제
 *
 * @see MatchSyncOrchestrator
 * @see MatchDataSyncDispatcher
 * @see JobSchedulerService
 */
@Component
class SimpleMatchDataSyncDispatcher(
    private val orchestrators: List<MatchSyncOrchestrator>,
    private val jobSchedulerService: JobSchedulerService,
) : MatchDataSyncDispatcher {
    private val log = logger()

    override fun syncByFixtureUid(
        fixtureUid: String,
        jobContext: JobContext?,
    ): MatchDataSyncResult {
        // 1. Orchestrator를 통해 동기화 수행
        val result = performSync(fixtureUid)

        // 2. JobContext가 있으면 Result에 따라 Job 관리
        if (jobContext != null) {
            manageJobTransition(fixtureUid, result, jobContext)
        }

        return result
    }

    /**
     * Orchestrator를 통해 동기화 수행
     */
    private fun performSync(fixtureUid: String): MatchDataSyncResult {
        for (orchestrator in orchestrators) {
            if (orchestrator.isSupport(fixtureUid)) {
                return orchestrator.syncMatchData(fixtureUid)
            }
        }
        log.warn("지원하는 Orchestrator를 찾지 못했습니다. fixtureUid=$fixtureUid")
        return MatchDataSyncResult.Error(
            message = "No orchestrator found for fixtureUid=$fixtureUid",
            kickoffTime = null,
        )
    }

    /**
     * Result에 따라 Job 전환 관리
     */
    private fun manageJobTransition(
        fixtureUid: String,
        result: MatchDataSyncResult,
        jobContext: JobContext,
    ) {
        when (result) {
            is MatchDataSyncResult.PreMatch -> {
                if (result.shouldTerminatePreMatchJob) {
                    log.info("PreMatch complete - removing PreMatchJob (LiveMatchJob already scheduled) - fixtureUid={}", fixtureUid)
                    jobSchedulerService.removeJob(jobContext.jobKey)
                    // LiveMatchJob은 이미 등록되어 있음, 추가 등록 안 함
                }
            }

            is MatchDataSyncResult.Live -> {
                if (result.isMatchFinished) {
                    log.info("LiveMatch → PostMatch transition - fixtureUid={}", fixtureUid)
                    jobSchedulerService.removeJob(jobContext.jobKey)
                    jobSchedulerService.addPostMatchJob(
                        fixtureUid = fixtureUid,
                        startTime = Instant.now(),
                    )
                }
            }

            is MatchDataSyncResult.PostMatch -> {
                if (result.shouldStopPolling) {
                    log.info("PostMatch polling complete - removing job for fixtureUid={}", fixtureUid)
                    jobSchedulerService.removeJob(jobContext.jobKey)
                }
            }

            is MatchDataSyncResult.Error -> {
                log.error(
                    "Match sync error - fixtureUid={}, phase={}, message={}",
                    fixtureUid,
                    jobContext.jobPhase,
                    result.message,
                )
                // Error 발생 시 Job은 계속 실행 (자동 재시도)
            }
        }
    }
}
