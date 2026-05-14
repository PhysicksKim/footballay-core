package com.footballay.core.infra.dispatcher.match

/**
 * Match data sync를 위한 인터페이스
 *
 * [com.footballay.core.infra.persistence.core.entity.FixtureCore] 의 uid 를 기반으로
 * 해당 경기의 Match data 를 저장합니다.
 *
 * @see MatchDataSyncResult
 */
interface MatchDataSyncDispatcher {
    /**
     * 주어진 fixture UID에 대해 지원 가능한 Provider를 찾아 라이브 경기 데이터를 sync합니다.
     *
     * @param fixtureUid 경기 고유 식별자
     * @return 다음 폴링 액션 지시사항
     */
    fun syncByFixtureUid(fixtureUid: String): MatchDataSyncResult
}
