package com.footballay.core.infra.facade

import com.footballay.core.infra.apisports.LeagueApiSportsSyncer
import com.footballay.core.infra.apisports.TeamApiSportsSyncer
import com.footballay.core.infra.apisports.PlayerApiSportsSyncer
import com.footballay.core.infra.apisports.LeagueApiSportsQueryService
import com.footballay.core.infra.apisports.dto.*
import com.footballay.core.infra.apisports.fetch.ApiSportsV3Fetcher
import com.footballay.core.infra.apisports.fetch.response.ApiSportsLeague
import com.footballay.core.infra.apisports.fetch.response.ApiSportsPlayer
import com.footballay.core.infra.apisports.fetch.response.ApiSportsTeam
import com.footballay.core.logger
import org.springframework.stereotype.Service

@Service
class ApiSportsSyncFacade(
    private val leagueSyncer: LeagueApiSportsSyncer,
    private val teamSyncer: TeamApiSportsSyncer,
    private val playerSyncer: PlayerApiSportsSyncer,
    private val fetcher: ApiSportsV3Fetcher,
    private val leagueQueryService: LeagueApiSportsQueryService
) {

    val log = logger()

    /**
     * 현재 시즌의 모든 리그를 Fetch하고 Sync합니다.
     * 
     * @return 처리된 리그 수
     */
    fun syncCurrentLeagues(): Int {
        log.info("Starting sync for current leagues")
        
        val fetchResponse = fetcher.fetchLeaguesCurrent()
        log.info("Fetched ${fetchResponse.response.size} leagues from ApiSports")
        
        val dtos = fetchResponse.response.map { mapToLeagueCreateDto(it) }
        leagueSyncer.saveLeagues(dtos)  
        
        log.info("Successfully synced ${dtos.size} leagues")
        return dtos.size
    }

    /**
     * 특정 리그의 팀들을 Fetch하고 Sync합니다.
     * 
     * @param leagueApiId 리그의 ApiSports ID
     * @param season 시즌 연도
     * @return 처리된 팀 수
     */
    fun syncTeamsOfLeague(leagueApiId: Long, season: Int): Int {
        log.info("Starting sync for teams of league. leagueApiId=$leagueApiId, season=$season")
        
        val fetchResponse = fetcher.fetchTeamsOfLeague(leagueApiId, season)
        log.info("Fetched ${fetchResponse.response.size} teams for league $leagueApiId")
        
        val dtos = fetchResponse.response.map { mapToTeamCreateDto(it) }
        teamSyncer.saveTeamsOfLeague(leagueApiId, dtos)
        
        log.info("Successfully synced ${dtos.size} teams for league $leagueApiId")
        return dtos.size
    }

    /**
     * 특정 팀의 선수들을 Fetch하고 Sync합니다.
     * 
     * @param teamApiId 팀의 ApiSports ID
     * @return 처리된 선수 수
     */
    fun syncPlayersOfTeam(teamApiId: Long): Int {
        log.info("Starting sync for players of team. teamApiId=$teamApiId")
        
        val fetchResponse = fetcher.fetchSquadOfTeam(teamApiId)
        log.info("Fetched ${fetchResponse.response.size} players for team $teamApiId")
        
        // 빈 응답인 경우 0 반환
        if (fetchResponse.response.isEmpty()) {
            log.info("No players found for team $teamApiId")
            return 0
        }
        
        // fetchSquadOfTeam은 팀별로 응답이 와서, response는 하나의 team과 그 team의 players를 포함합니다
        val teamResponse = fetchResponse.response.first()
            
        val dtos = teamResponse.players.map { mapToPlayerCreateDto(it) }
        playerSyncer.syncPlayersOfTeam(teamApiId, dtos)
        
        log.info("Successfully synced ${dtos.size} players for team $teamApiId")
        return dtos.size
    }

    /**
     * 특정 리그의 현재 시즌으로 팀들을 Fetch하고 Sync합니다.
     * 리그의 currentSeason을 자동으로 조회하여 사용합니다.
     * 
     * @param leagueApiId 리그의 ApiSports ID
     * @return 처리된 팀 수
     */
    fun syncTeamsOfLeagueWithCurrentSeason(leagueApiId: Long): Int {
        log.info("Starting sync teams of league with current season for leagueApiId: $leagueApiId")
        
        // 리그 정보 조회하여 현재 시즌 확인
        val leagueInfo = leagueQueryService.findByApiIdOrThrow(leagueApiId)
        val currentSeason = leagueInfo.getCurrentSeasonOrThrow()
        
        log.info("Found league '${leagueInfo.name}' with current season: $currentSeason")
        
        // 기존 syncTeamsOfLeague 메서드 호출
        val syncedCount = syncTeamsOfLeague(leagueApiId, currentSeason)
        
        log.info("Successfully synced $syncedCount teams for league '${leagueInfo.name}' (apiId: $leagueApiId) with current season: $currentSeason")
        return syncedCount
    }

    // ==================== Private Mapping Methods ====================

    private fun mapToLeagueCreateDto(response: ApiSportsLeague.Current): LeagueApiSportsCreateDto {
        log.debug("Mapping league: ${response.league.name} (${response.league.id})")
        
        return LeagueApiSportsCreateDto(
            apiId = response.league.id.toLong(),
            name = response.league.name,
            type = response.league.type,
            logo = response.league.logo,
            countryName = response.country.name,
            countryCode = response.country.code,
            countryFlag = response.country.flag,
            currentSeason = response.seasons.find { it.current == true }?.year,
            seasons = response.seasons.map { mapToSeasonCreateDto(it) }
        )
    }

    private fun mapToSeasonCreateDto(season: ApiSportsLeague.Current.SeasonInfo): LeagueApiSportsSeasonCreateDto {
        return LeagueApiSportsSeasonCreateDto(
            seasonYear = season.year,
            seasonStart = season.start,
            seasonEnd = season.end,
            coverage = season.coverage?.let { mapToCoverageCreateDto(it) }
        )
    }

    private fun mapToCoverageCreateDto(coverage: ApiSportsLeague.Current.SeasonInfo.CoverageInfo): LeagueApiSportsCoverageCreateDto {
        return LeagueApiSportsCoverageCreateDto(
            fixturesEvents = coverage.fixtures?.events,
            fixturesLineups = coverage.fixtures?.lineups,
            fixturesStatistics = coverage.fixtures?.statistics_fixtures,
            fixturesPlayers = coverage.fixtures?.statistics_players,
            standings = coverage.standings,
            players = coverage.players,
            topScorers = coverage.top_scorers,
            topAssists = coverage.top_assists,
            topCards = coverage.top_cards,
            injuries = coverage.injuries,
            predictions = coverage.predictions,
            odds = coverage.odds
        )
    }

    private fun mapToTeamCreateDto(response: ApiSportsTeam.OfLeague): TeamApiSportsCreateDto {
        log.debug("Mapping team: ${response.team.name} (${response.team.id})")
        
        return TeamApiSportsCreateDto(
            apiId = response.team.id.toLong(),
            name = response.team.name,
            code = response.team.code,
            country = response.team.country,
            founded = response.team.founded,
            national = response.team.national,
            logo = response.team.logo,
            venue = mapToVenueCreateDto(response.venue)
        )
    }

    private fun mapToVenueCreateDto(venue: ApiSportsTeam.OfLeague.VenueDetail): VenueApiSportsCreateDto {
        return VenueApiSportsCreateDto(
            apiId = venue.id.toLong(),
            name = venue.name,
            address = venue.address,
            city = venue.city,
            capacity = venue.capacity,
            surface = venue.surface,
            image = venue.image
        )
    }

    private fun mapToPlayerCreateDto(player: ApiSportsPlayer.OfTeam.PlayerInfo): PlayerApiSportsCreateDto {
        log.debug("Mapping player: ${player.name} (${player.id})")
        
        return PlayerApiSportsCreateDto(
            apiId = player.id.toLong(),
            name = player.name,
            age = player.age,
            number = player.number,
            position = player.position,
            photo = player.photo
        )
    }
}