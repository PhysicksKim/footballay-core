package com.footballay.core.infra.facade

/**
 * ApiSports Backbone 데이터 동기화를 담당하는 인터페이스
 * 
 * **주요 책임:**
 * - ApiSports API ID 기반 backbone 데이터 동기화
 * - 리그, 팀, 선수, 경기일정 데이터 저장
 * - Admin Web Service에서 호출되는 동기화 작업
 * 
 * **동기화 대상:**
 * - Current Leagues: 현재 시즌의 모든 리그
 * - Teams of League: 특정 리그의 팀들
 * - Players of Team: 특정 팀의 선수들
 * - Fixtures of League: 특정 리그의 경기일정
 */
interface ApiSportsBackboneSyncFacade {

    /**
     * 현재 시즌의 모든 리그를 Fetch하고 Sync합니다.
     * 
     * @return 처리된 리그 수
     */
    fun syncCurrentLeagues(): Int

    /**
     * 특정 리그의 팀들을 Fetch하고 Sync합니다.
     * 
     * @param leagueApiId 리그의 ApiSports ID
     * @param season 시즌 연도
     * @return 처리된 팀 수
     */
    fun syncTeamsOfLeague(leagueApiId: Long, season: Int): Int

    /**
     * 특정 팀의 선수들을 Fetch하고 Sync합니다.
     * 
     * @param teamApiId 팀의 ApiSports ID
     * @return 처리된 선수 수
     */
    fun syncPlayersOfTeam(teamApiId: Long): Int

    /**
     * 특정 리그의 현재 시즌으로 팀들을 Fetch하고 Sync합니다.
     * 리그의 currentSeason을 자동으로 조회하여 사용합니다.
     * 
     * @param leagueApiId 리그의 ApiSports ID
     * @return 처리된 팀 수
     */
    fun syncTeamsOfLeagueWithCurrentSeason(leagueApiId: Long): Int

    /**
     * 특정 리그의 현재 시즌으로 경기일정을 Fetch하고 Sync합니다.
     * 
     * @param leagueApiId 리그의 ApiSports ID
     * @param season 시즌 연도
     * @return 처리된 경기일정 수
     */
    fun syncFixturesOfLeagueWithSeason(leagueApiId: Long, season: Int): Int
} 