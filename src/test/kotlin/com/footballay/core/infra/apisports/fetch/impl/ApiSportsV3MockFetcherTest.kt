package com.footballay.core.infra.apisports.fetch.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("dev", "mockapi")
class ApiSportsV3MockFetcherTest {

    @Autowired
    lateinit var mockFetcher: ApiSportsV3MockFetcher

    @Test
    fun `fetchStatus는 목 상태 데이터를 반환해야 한다`() {
        // when
        val result = mockFetcher.fetchStatus()

        // then
        assertThat(result).isNotNull
        assertThat(result.get).isEqualTo("status")
        assertThat(result.results).isEqualTo(1)
        assertThat(result.response.account.firstname).isEqualTo("Mock")
        assertThat(result.response.account.lastname).isEqualTo("User")
        assertThat(result.response.subscription.active).isTrue()
    }

    @Test
    fun `fetchLeaguesCurrent는 지원되는 리그들을 반환해야 한다`() {
        // when
        val result = mockFetcher.fetchLeaguesCurrent()

        // then
        assertThat(result).isNotNull
        assertThat(result.get).isEqualTo("leagues")
        assertThat(result.results).isEqualTo(2)
        assertThat(result.response).hasSize(2)
        
        val premierLeague = result.response.find { it.league.id == ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID.toInt() }
        assertThat(premierLeague).isNotNull
        assertThat(premierLeague!!.league.name).isEqualTo("Premier League")
        assertThat(premierLeague.country.name).isEqualTo("England")
        assertThat(premierLeague.seasons).isNotEmpty
        assertThat(premierLeague.seasons.any { it.current == true }).isTrue()
    }

    @Test
    fun `fetchTeamsOfLeague는 지원되는 리그와 시즌에 대해 팀들을 반환해야 한다`() {
        // given
        val supportedLeagueId = ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID
        val supportedSeason = ApiSportsV3MockFetcher.SUPPORTED_SEASON

        // when
        val result = mockFetcher.fetchTeamsOfLeague(supportedLeagueId, supportedSeason)

        // then
        assertThat(result).isNotNull
        assertThat(result.get).isEqualTo("teams")
        assertThat(result.results).isGreaterThan(0)
        assertThat(result.response).isNotEmpty
        
        val manchesterCity = result.response.find { it.team.id == ApiSportsV3MockFetcher.SUPPORTED_TEAM_ID.toInt() }
        assertThat(manchesterCity).isNotNull
        assertThat(manchesterCity!!.team.name).isEqualTo("Manchester City")
        assertThat(manchesterCity.team.code).isEqualTo("MCI")
        assertThat(manchesterCity.venue).isNotNull
    }

    @Test
    fun `fetchTeamsOfLeague는 지원되지 않는 리그에 대해 빈 결과를 반환해야 한다`() {
        // given
        val unsupportedLeagueId = 999L
        val supportedSeason = ApiSportsV3MockFetcher.SUPPORTED_SEASON

        // when
        val result = mockFetcher.fetchTeamsOfLeague(unsupportedLeagueId, supportedSeason)

        // then
        assertThat(result).isNotNull
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }

    @Test
    fun `fetchTeamsOfLeague는 지원되지 않는 시즌에 대해 빈 결과를 반환해야 한다`() {
        // given
        val supportedLeagueId = ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID
        val unsupportedSeason = 2023

        // when
        val result = mockFetcher.fetchTeamsOfLeague(supportedLeagueId, unsupportedSeason)

        // then
        assertThat(result).isNotNull
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }

    @Test
    fun `fetchSquadOfTeam은 지원되는 팀에 대해 선수들을 반환해야 한다`() {
        // given
        val supportedTeamId = ApiSportsV3MockFetcher.SUPPORTED_TEAM_ID

        // when
        val result = mockFetcher.fetchSquadOfTeam(supportedTeamId)

        // then
        assertThat(result).isNotNull
        assertThat(result.get).isEqualTo("players/squads")
        assertThat(result.results).isGreaterThan(0)
        assertThat(result.response).isNotEmpty
        
        val teamResponse = result.response.first()
        assertThat(teamResponse.team.id).isEqualTo(supportedTeamId.toInt())
        assertThat(teamResponse.team.name).isEqualTo("Manchester City")
        assertThat(teamResponse.players).isNotEmpty
        
        val erlingHaaland = teamResponse.players.find { it.name == "Erling Haaland" }
        assertThat(erlingHaaland).isNotNull
        assertThat(erlingHaaland!!.position).isEqualTo("Attacker")
        assertThat(erlingHaaland.age).isEqualTo(23)
    }

    @Test
    fun `fetchSquadOfTeam은 지원되지 않는 팀에 대해 빈 결과를 반환해야 한다`() {
        // given
        val unsupportedTeamId = 999L

        // when
        val result = mockFetcher.fetchSquadOfTeam(unsupportedTeamId)

        // then
        assertThat(result).isNotNull
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }

    @Test
    fun `fetchFixturesOfLeague는 현재 빈 결과를 반환해야 한다`() {
        // given
        val leagueId = ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID
        val season = ApiSportsV3MockFetcher.SUPPORTED_SEASON.toString()

        // when
        val result = mockFetcher.fetchFixturesOfLeague(leagueId, season)

        // then
        assertThat(result).isNotNull
        assertThat(result.get).isEqualTo("fixtures")
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }

    @Test
    fun `fetchFixtureSingle은 현재 빈 결과를 반환해야 한다`() {
        // given
        val fixtureId = 123456L

        // when
        val result = mockFetcher.fetchFixtureSingle(fixtureId)

        // then
        assertThat(result).isNotNull
        assertThat(result.get).isEqualTo("fixtures")
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }
} 