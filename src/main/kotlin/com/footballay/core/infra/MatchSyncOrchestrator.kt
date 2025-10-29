package com.footballay.core.infra

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult

/**
 * Fixture Uid 기반으로 Match data sync 작업을 수행하는 Orchestrator 입니다.
 * Uid 별로 지원하는 Orchestrator가 다를 수 있으므로 [isSupport] 함수를 통해 구분합니다.
 *
 * 이 인터페이스를 구현할 때, [com.footballay.core.infra.persistence.core.entity.FixtureCore.uid] 를 기반으로
 * 해당 Fixture Match Data sync 를 지원하는지 판단하고, 지원하는 경우 [syncMatchData] 함수를 통해 sync 작업이 시작됩니다.
 *
 * **핵심 책임:**
 * - 해당 구현체가 Fixture Uid Match Data Sync를 지원하는지 판단
 * - Match data sync 작업 수행 및 결과 반환
 * - [MatchDataSyncResult] 는
 *
 * **결과 반환 유의사항**
 * [MatchSyncOrchestrator] 는 동기화 작업 후 반드시 [MatchDataSyncResult] 를 반환해야 합니다.
 * [MatchDataSyncResult] 는 다양한 호출자가 다음 동작(추가 폴링 or 종료)을 결정하는 데 사용됩니다.
 * 따라서 단순히 저장 성공 응답이 아니라 경기 상태 정보와 같은 구체적인 정보도 포함합니다.
 *
 * **동작 흐름:**
 * 1. Dispatcher가 `isSupport(uid)` 호출로 지원 여부 확인
 * 2. 지원하는 경우 `syncMatchData(uid)` 호출
 * 3. Provider API에서 데이터 조회 및 동기화
 * 4. `MatchDataSyncResult` 반환
 *
 * @see MatchDataSyncResult
 */
interface MatchSyncOrchestrator {
    /**
     * 해당 fixture Match Sync가 지원되는지 여부를 판단합니다.
     *
     * @param uid 경기 고유 식별자 (FixtureCore.uid)
     * @return 지원 여부
     */
    fun isSupport(uid: String): Boolean

    /**
     * 해당 fixture의 Match Data 를 sync 합니다.
     *
     * **주의:** `isSupport(uid)`가 true를 반환한 경우에만 호출되어야 합니다.
     *
     * @param uid 경기 고유 식별자 (FixtureCore.uid)
     * @return 동기화 결과 (경기 종료 여부, 킥오프 시간 등)
     */
    fun syncMatchData(uid: String): MatchDataSyncResult
}
