package com.footballay.core.infra.apisports.fetch.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.infra.apisports.fetch.ApiSportsV3Fetcher
import com.footballay.core.infra.apisports.fetch.response.*
import com.footballay.core.logger
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Mock implementation of [ApiSportsV3Fetcher] that provides test data.
 * 
 * This implementation is used for testing to avoid real API calls.
 * Activate this by using the 'mockapi' profile.
 * 
 * Supported test data:
 * - League 39 (Premier League), Season 2024: 20 teams
 * - Team 50 (Manchester City): Squad with players
 * - Current leagues: Premier League and La Liga
 */
@Profile("mockapi")
@Component
class ApiSportsV3MockFetcher(
    private val objectMapper: ObjectMapper
) : ApiSportsV3Fetcher {

    private val log = logger()

    companion object {
        // Supported mock data constants for documentation
        const val SUPPORTED_LEAGUE_ID = 39L  // Premier League
        const val SUPPORTED_SEASON = 2024
        const val SUPPORTED_TEAM_ID = 50L    // Manchester City
    }

    override fun fetchStatus(): ApiSportsV3LiveStatusEnvelope<ApiSportsStatus> {
        log.info("Mock fetching API status")
        
        val statusResponse = ApiSportsStatus(
            account = ApiSportsStatus.Account(
                firstname = "Mock",
                lastname = "User",
                email = "mock@test.com"
            ),
            subscription = ApiSportsStatus.Subscription(
                plan = "Mock Plan",
                end = "2025-12-31",
                active = true
            ),
            requests = ApiSportsStatus.Requests(
                current = 0,
                limit_day = 1000
            )
        )
        
        return ApiSportsV3LiveStatusEnvelope(
            get = "status",
            parameters = emptyList(),
            errors = emptyList(),
            results = 1,
            paging = Paging(current = 1, total = 1),
            response = statusResponse
        )
    }

    override fun fetchLeaguesCurrent(): ApiSportsV3Envelope<ApiSportsLeague.Current> {
        log.info("Mock fetching current leagues")
        
        // Create mock response for current leagues
        val premierLeague = ApiSportsLeague.Current(
            league = ApiSportsLeague.Current.LeagueInfo(
                id = 39,
                name = "Premier League",
                type = "League",
                logo = "https://media.api-sports.io/football/leagues/39.png"
            ),
            country = ApiSportsLeague.Current.CountryInfo(
                name = "England",
                code = "GB",
                flag = "https://media.api-sports.io/flags/gb.svg"
            ),
            seasons = listOf(
                ApiSportsLeague.Current.SeasonInfo(
                    year = 2024,
                    start = "2024-08-16",
                    end = "2025-05-25",
                    current = true,
                    coverage = null
                )
            )
        )
        
        val laLiga = ApiSportsLeague.Current(
            league = ApiSportsLeague.Current.LeagueInfo(
                id = 140,
                name = "La Liga", 
                type = "League",
                logo = "https://media.api-sports.io/football/leagues/140.png"
            ),
            country = ApiSportsLeague.Current.CountryInfo(
                name = "Spain",
                code = "ES", 
                flag = "https://media.api-sports.io/flags/es.svg"
            ),
            seasons = listOf(
                ApiSportsLeague.Current.SeasonInfo(
                    year = 2024,
                    start = "2024-08-18",
                    end = "2025-05-25",
                    current = true,
                    coverage = null
                )
            )
        )
        
        return ApiSportsV3Envelope(
            get = "leagues",
            parameters = mapOf("current" to "true"),
            errors = emptyList(),
            results = 2,
            paging = Paging(current = 1, total = 1),
            response = listOf(premierLeague, laLiga)
        )
    }

    override fun fetchTeamsOfLeague(leagueApiId: Long, season: Int): ApiSportsV3Envelope<ApiSportsTeam.OfLeague> {
        log.info("Mock fetching teams for league: $leagueApiId, season: $season")
        
        // For now, only support Premier League 2024
        if (leagueApiId != SUPPORTED_LEAGUE_ID || season != SUPPORTED_SEASON) {
            log.warn("Mock data only available for league $SUPPORTED_LEAGUE_ID (Premier League) season $SUPPORTED_SEASON. " +
                    "Requested: league $leagueApiId, season $season")
            return ApiSportsV3Envelope(
                get = "teams",
                parameters = mapOf("league" to leagueApiId.toString(), "season" to season.toString()),
                errors = emptyList(),
                results = 0,
                paging = Paging(current = 1, total = 1),
                response = emptyList()
            )
        }
        
        // Create mock teams for Premier League
        val teams = createMockPremierLeagueTeams()
        
        return ApiSportsV3Envelope(
            get = "teams",
            parameters = mapOf("league" to leagueApiId.toString(), "season" to season.toString()),
            errors = emptyList(),
            results = teams.size,
            paging = Paging(current = 1, total = 1),
            response = teams
        )
    }

    override fun fetchSquadOfTeam(teamApiId: Long): ApiSportsV3Envelope<ApiSportsPlayer.OfTeam> {
        log.info("Mock fetching squad for team: $teamApiId")
        
        // For now, only support Manchester City
        if (teamApiId != SUPPORTED_TEAM_ID) {
            log.warn("Mock data only available for team $SUPPORTED_TEAM_ID (Manchester City). " +
                    "Requested: team $teamApiId")
            return ApiSportsV3Envelope(
                get = "players/squads",
                parameters = mapOf("team" to teamApiId.toString()),
                errors = emptyList(),
                results = 0,
                paging = Paging(current = 1, total = 1),
                response = emptyList()
            )
        }
        
        val players = createMockManchesterCityPlayers()
        
        return ApiSportsV3Envelope(
            get = "players/squads",
            parameters = mapOf("team" to teamApiId.toString()),
            errors = emptyList(),
            results = players.size,
            paging = Paging(current = 1, total = 1),
            response = players
        )
    }

    override fun fetchFixturesOfLeague(leagueApiId: Long, season: String): ApiSportsV3Envelope<ApiSportsFixture.OfLeague> {
        log.info("Mock fetching fixtures for league: $leagueApiId, season: $season")
        
        return ApiSportsV3Envelope(
            get = "fixtures",
            parameters = mapOf("league" to leagueApiId.toString(), "season" to season),
            errors = emptyList(),
            results = 0,
            paging = Paging(current = 1, total = 1),
            response = emptyList()
        )
    }

    override fun fetchFixtureSingle(fixtureApiId: Long): ApiSportsV3Envelope<ApiSportsFixture.Single> {
        log.info("Mock fetching single fixture: $fixtureApiId")
        
        return ApiSportsV3Envelope(
            get = "fixtures",
            parameters = mapOf("id" to fixtureApiId.toString()),
            errors = emptyList(),
            results = 0,
            paging = Paging(current = 1, total = 1),
            response = emptyList()
        )
    }

    private fun createMockPremierLeagueTeams(): List<ApiSportsTeam.OfLeague> {
        return listOf(
            // Manchester City (ID: 50)
            createMockTeam(50, "Manchester City", "MCI", "England", 1880, false),
            // Arsenal (ID: 42) 
            createMockTeam(42, "Arsenal", "ARS", "England", 1886, false),
            // Liverpool (ID: 40)
            createMockTeam(40, "Liverpool", "LIV", "England", 1892, false),
            // Chelsea (ID: 49)
            createMockTeam(49, "Chelsea", "CHE", "England", 1905, false),
            // Tottenham (ID: 47)
            createMockTeam(47, "Tottenham", "TOT", "England", 1882, false)
        )
    }

    private fun createMockTeam(id: Int, name: String, code: String, country: String, founded: Int, national: Boolean): ApiSportsTeam.OfLeague {
        return ApiSportsTeam.OfLeague(
            team = ApiSportsTeam.OfLeague.TeamDetail(
                id = id,
                name = name,
                code = code,
                country = country,
                founded = founded,
                national = national,
                logo = "https://media.api-sports.io/football/teams/$id.png"
            ),
            venue = ApiSportsTeam.OfLeague.VenueDetail(
                id = id * 100,
                name = "$name Stadium",
                address = "$name Address",
                city = country,
                capacity = 50000,
                surface = "grass",
                image = "https://media.api-sports.io/football/venues/${id * 100}.png"
            )
        )
    }

    private fun createMockManchesterCityPlayers(): List<ApiSportsPlayer.OfTeam> {
        val teamInfo = ApiSportsPlayer.OfTeam.TeamInfo(
            id = SUPPORTED_TEAM_ID.toInt(),
            name = "Manchester City",
            logo = "https://media.api-sports.io/football/teams/$SUPPORTED_TEAM_ID.png"
        )
        
        val players = listOf(
            createMockPlayerInfo(1, "Ederson", "Goalkeeper", 30),
            createMockPlayerInfo(2, "Kyle Walker", "Defender", 33),
            createMockPlayerInfo(3, "Erling Haaland", "Attacker", 23),
            createMockPlayerInfo(4, "Kevin De Bruyne", "Midfielder", 32),
            createMockPlayerInfo(5, "Phil Foden", "Midfielder", 23)
        )
        
        return listOf(
            ApiSportsPlayer.OfTeam(
                team = teamInfo,
                players = players
            )
        )
    }

    private fun createMockPlayerInfo(id: Int, name: String, position: String, age: Int): ApiSportsPlayer.OfTeam.PlayerInfo {
        return ApiSportsPlayer.OfTeam.PlayerInfo(
            id = id,
            name = name,
            age = age,
            number = id,
            position = position,
            photo = "https://media.api-sports.io/football/players/$id.png"
        )
    }
} 