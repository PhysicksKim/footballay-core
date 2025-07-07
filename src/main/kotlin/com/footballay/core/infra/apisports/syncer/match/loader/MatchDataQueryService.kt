package com.footballay.core.infra.apisports.syncer.match.loader

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam

/**
 * Live 경기 데이터 조회를 담당하는 서비스
 * 
 * 3개의 최적화된 쿼리로 필요한 데이터를 효율적으로 로딩합니다.
 * Team Stats는 소량의 중복을 허용하고 트랜잭션 왕복 횟수를 줄입니다.
 */
interface MatchDataQueryService {
    
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