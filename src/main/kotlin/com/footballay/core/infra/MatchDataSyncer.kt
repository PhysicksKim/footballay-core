package com.footballay.core.infra

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult

/**
 * 특정 Provider의 라이브 매치 데이터 동기화를 담당하는 인터페이스
 *
 * 각 Provider(ApiSports, 다른 API 등)별로 독립적인 구현체를 제공하여
 * 라이브 매치 데이터를 동기화합니다.
 *
 * **핵심 책임:**
 * - Provider별 지원 여부 판단 (`isSupport`)
 * - 라이브 데이터 동기화 수행 (`syncMatchData`)
 * - 다음 폴링 액션 지시사항 반환
 *
 * **구현 예시:**
 * - `ApiSportsMatchSyncFacade`: ApiSports API 기반 동기화
 * - `OtherProviderMatchSyncFacade`: 다른 Provider API 기반 동기화
 *
 * **동작 흐름:**
 * 1. `FetcherProviderResolver`가 `isSupport(uid)` 호출
 * 2. 지원하는 경우 `syncMatchData(uid)` 호출
 * 3. Provider별 동기화 로직 수행
 * 4. `ActionAfterMatchSync` 반환
 *
 * @see FetcherProviderResolver
 * @see MatchDataSyncResult
 *
 * AI가 작성한 주석
 */
interface MatchDataSyncer {

    /**
     * uid 기반으로 해당 fixture가 이 Provider에 의해 지원되는지 여부를 판단합니다.
     *
     * **판단 기준:**
     * - Provider Health Check 상태
     * - Fallback Flag 설정
     * - Runtime 동적 설정
     * - Provider별 고유 로직
     *
     * @param uid 경기 고유 식별자
     * @return 지원 여부
     */
    fun isSupport(uid: String): Boolean

    /**
     * uid를 통해 해당 fixture의 실시간 데이터를 동기화합니다.
     *
     * **주의사항:**
     * 이 메서드는 해당 fixture가 이 Provider에 의해 지원되는 경우에만 호출되어야 합니다.
     * `isSupport(uid)`가 `true`를 반환한 후에만 호출하세요.
     *
     * **동기화 과정:**
     * 1. Provider API에서 라이브 데이터 조회
     * 2. 선수 사전 저장 (필요시)
     * 3. 매치 데이터 동기화
     * 4. 다음 폴링 액션 결정
     *
     * @param uid 경기 고유 식별자
     * @return 다음 폴링 액션 지시사항
     */
    fun syncMatchData(uid: String): MatchDataSyncResult

}