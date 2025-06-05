package com.footballay.core.infra.provider

import com.footballay.core.infra.provider.dto.*

/**
 * 외부 API에서 가져오는 리그/팀/선수/경기 정보를 추상화한 인터페이스
 * - API마다 파라미터/엔드포인트가 다를 수 있음
 * - 하나의 메서드 안에서 여러 HTTP 호출이 이뤄져도 괜찮음
 */
interface FootballDataProvider {
    
    /**
     * 현재 시즌 중인 모든 리그 정보 조회
     */
    fun fetchAllCurrentLeagues(): List<ApiLeagueDto>

    /**
     * 단일 리그 상세 정보 조회 (current=true 포함)
     */
    fun fetchLeagueById(leagueId: Long): ApiLeagueDto

    /**
     * 특정 리그의 팀 목록 조회
     */
    fun fetchTeamsOfLeague(leagueId: Long, season: Int): List<ApiTeamDto>

    /**
     * 특정 팀의 선수 목록 조회
     */
    fun fetchPlayersOfTeam(teamId: Long): List<ApiPlayerDto>

    /**
     * 특정 리그/시즌의 경기 일정 조회
     */
    fun fetchFixturesOfLeagueSeason(leagueId: Long, season: Int): List<ApiFixtureDto>
} 