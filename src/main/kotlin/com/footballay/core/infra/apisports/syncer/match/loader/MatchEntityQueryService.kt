package com.footballay.core.infra.apisports.syncer.match.loader

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam

/**
 * Live 경기 엔티티 조회를 담당하는 서비스
 * 
 * 영속 상태의 엔티티들을 직접 반환하여 EntityBundle 구성에 사용됩니다.
 * 3개의 최적화된 쿼리로 필요한 엔티티들을 효율적으로 로딩합니다.
 * 
 * **DTO vs Entity 구분:**
 * - 이 서비스는 영속 상태의 엔티티를 반환 (EntityBundle용)
 * - DTO 변환이 필요한 경우 별도의 DTO 서비스를 사용
 */
interface MatchEntityQueryService {
    
    /**
     * 1. Home Team + Team Statistics + Players + Player Statistics 조회
     * 
     * ApiSportsMatchTeam(home) + ApiSportsMatchTeamStatistics + 
     * ApiSportsMatchPlayer[] + ApiSportsMatchPlayerStatistics[]
     * 
     * Team Stats는 모든 Player 행에 중복되지만 트랜잭션 왕복 절약
     */
    fun loadHomeTeamWithPlayersAndStats(fixtureApiId: Long): ApiSportsMatchTeam?
    
    /**
     * 2. Away Team + Team Statistics + Players + Player Statistics 조회
     *  
     * ApiSportsMatchTeam(away) + ApiSportsMatchTeamStatistics + 
     * ApiSportsMatchPlayer[] + ApiSportsMatchPlayerStatistics[]
     */
    fun loadAwayTeamWithPlayersAndStats(fixtureApiId: Long): ApiSportsMatchTeam?
    
    /**
     * 3. Fixture 핵심 데이터 + Events 조회
     * 
     * FixtureApiSports + ApiSportsMatchEvent[] (player, assist 포함)
     */
    fun loadFixtureWithEvents(fixtureApiId: Long): FixtureApiSports?

} 