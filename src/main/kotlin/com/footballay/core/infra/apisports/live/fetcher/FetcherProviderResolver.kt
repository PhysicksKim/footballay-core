package com.footballay.core.infra.apisports.live.fetcher

import com.footballay.core.infra.apisports.live.ActionAfterMatchSync

/**
 * 라이브 매치 데이터 동기화를 위한 Provider Resolver 인터페이스
 * 
 * 등록된 LiveMatchDataSyncer 구현체들 중에서 적절한 Provider를 선택하여
 * 라이브 데이터 동기화를 수행합니다.
 */
interface FetcherProviderResolver {
    
    /**
     * 주어진 fixture UID에 대해 지원 가능한 Provider를 찾아 라이브 데이터를 동기화합니다.
     * 
     * @param fixtureUid 경기 고유 식별자
     * @return 다음 폴링 액션 지시사항, 지원하는 Provider가 없으면 null
     */
    fun fetchLiveData(fixtureUid: String): ActionAfterMatchSync?
}