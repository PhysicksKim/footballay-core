package com.footballay.core.infra.facade.fetcher

import com.footballay.core.infra.facade.fetcher.ActionAfterMatchSync

/**
 * 라이브 매치 데이터 동기화를 위한 Provider Resolver 인터페이스
 * 
 * 등록된 [MatchDataSyncer] 구현체들 중에서 적절한 Provider를 선택하여
 * 라이브 데이터 동기화를 수행합니다.
 * 
 * **동작 방식:**
 * 1. `fixtureUid`를 기반으로 지원 가능한 Provider 탐색
 * 2. 적절한 Provider의 `syncMatchData()` 호출
 * 3. 다음 폴링 액션 지시사항 반환
 * 
 * **사용 예시:**
 * ```kotlin
 * val action = fetcherProviderResolver.fetchLiveData("fixture-123")
 * when (action) {
 *     is ActionAfterMatchSync.Ongoing -> scheduleNextPoll(action.kickoffTime)
 *     is ActionAfterMatchSync.Completed -> stopPolling()
 * }
 * ```
 * 
 * @see MatchDataSyncer
 * @see ActionAfterMatchSync
 * 
 * AI가 작성한 주석
 */
interface FetcherProviderResolver {
    
    /**
     * 주어진 fixture UID에 대해 지원 가능한 Provider를 찾아 라이브 경기 데이터를 동기화합니다.
     * 
     * @param fixtureUid 경기 고유 식별자
     * @return 다음 폴링 액션 지시사항, 지원하는 Provider가 없으면 null
     */
    fun fetchMatchData(fixtureUid: String): ActionAfterMatchSync?
}