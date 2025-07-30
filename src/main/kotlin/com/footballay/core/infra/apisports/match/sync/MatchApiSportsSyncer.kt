package com.footballay.core.infra.apisports.match.sync

import com.footballay.core.infra.apisports.match.live.ActionAfterMatchSync
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsV3Envelope
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsFixture
import com.footballay.core.infra.apisports.match.live.FullMatchSyncDto

typealias ApiSportsFixtureSingle = ApiSportsV3Envelope<ApiSportsFixture.Single>

/**
 * ApiSports Live 매치 데이터 동기화 서비스 인터페이스
 * 
 * ApiSports API를 통해 받은 라이브 매치 데이터를 
 * 시스템 내부 엔티티로 동기화하는 책임을 담당합니다.
 * 
 * 주요 동기화 대상:
 * - 경기 상태 (진행중, 종료 등)
 * - 매치 이벤트 (골, 카드, 교체 등)L
 * - 팀/선수 통계
 * - 실시간 스코어
 */
interface MatchApiSportsSyncer {
    
    /**
     * ApiSports 라이브 매치 데이터를 전체 동기화합니다.
     * 
     * @param response ApiSports API 응답 데이터
     * @return 다음 폴링 액션 지시사항
     */
    fun syncFixtureMatchEntities(dto: FullMatchSyncDto): ActionAfterMatchSync
} 