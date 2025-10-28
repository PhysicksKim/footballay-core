package com.footballay.core.infra.apisports.match.sync

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsV3Envelope
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsFixture
import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto

typealias ApiSportsFixtureSingle = ApiSportsV3Envelope<ApiSportsFixture.Single>

/**
 * ApiSports Provider 의 Match data 를 sync 합니다.
 *
 * [FullMatchSyncDto] 를 받아서 Match 관련 엔티티로 저장합니다.
 * Match Sync 특성상 API 응답이 크고 긴 트랜잭션이 필요할 수 있으므로 성능을 고려해야 합니다.
 * 성능을 모니터링 하고, 클라이언트에 의한 호출이나 빠르게 반복해서 호출하지 마세요.
 *
 * @see com.footballay.core.infra.apisports.match
 */
interface ApiSportsMatchEntitySyncFacade {

    /**
     * 라이브 매치 데이터를 전체 동기화합니다.
     *
     * @see FullMatchSyncDto
     * @see MatchDataSyncResult
     * @param dto ApiSports 매치 데이터 DTO
     * @return 동기화 결과
     */
    fun syncFixtureMatchEntities(dto: FullMatchSyncDto): MatchDataSyncResult
} 