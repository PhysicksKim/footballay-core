package com.footballay.core.infra.dispatcher.match

import com.footballay.core.infra.MatchSyncOrchestrator
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * 매치 데이터 동기화 Dispatcher 기본 구현체
 *
 * 등록된 MatchSyncOrchestrator 구현체들을 순회하며,
 * 해당 fixture를 지원하는 Provider를 찾아 동기화를 수행합니다.
 *
 * **동작 방식:**
 * 1. 모든 Orchestrator에 대해 `isSupport(fixtureUid)` 호출
 * 2. 지원하는 Orchestrator를 찾으면 `syncMatchData(fixtureUid)` 호출
 * 3. 지원하는 Orchestrator가 없으면 fallback 결과 반환
 *
 * @see MatchSyncOrchestrator
 * @see MatchDataSyncDispatcher
 */
@Component
class SimpleMatchDataSyncDispatcher(
    private val orchestrators: List<MatchSyncOrchestrator>,
) : MatchDataSyncDispatcher {
    private val log = logger()

    override fun syncByFixtureUid(fixtureUid: String): MatchDataSyncResult {
        for (orchestrator in orchestrators) {
            if (orchestrator.isSupport(fixtureUid)) {
                return orchestrator.syncMatchData(fixtureUid)
            }
        }
        log.warn("지원하는 Orchestrator를 찾지 못했습니다. fixtureUid=$fixtureUid")
        return MatchDataSyncResult.ongoing(OffsetDateTime.now().plusHours(1))
    }
}
