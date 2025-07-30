package com.footballay.core.infra.apisports.shared.fetch.impl

import com.footballay.core.infra.apisports.shared.fetch.impl.ApiSportsV3MockFetcher
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.springframework.test.context.ActiveProfiles
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.assertj.core.api.Assertions.assertThat

@SpringBootTest
@ActiveProfiles("mockapi")
@DisplayName("ApiSportsV3MockFetcher JSON 파일 기반 테스트")
class ApiSportsV3MockFetcherJsonTest {

    @Autowired
    private lateinit var mockFetcher: ApiSportsV3MockFetcher

    @Test
    @DisplayName("fixture_1208021.json 파일을 읽어서 fixtureSingle 응답을 반환한다")
    fun `fixture_1208021_json_파일_읽기_테스트`() {
        // Given
        val fixtureId = 1208021L

        // When
        val result = mockFetcher.fetchFixtureSingle(fixtureId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result.get).isEqualTo("fixtures")
        assertThat(result.results).isEqualTo(1)
        assertThat(result.response).hasSize(1)
        
        val fixture = result.response.first()
        assertThat(fixture.fixture).isNotNull()
        assertThat(fixture.fixture.id).isEqualTo(fixtureId)
        assertThat(fixture.league).isNotNull()
        assertThat(fixture.teams).isNotNull()
        assertThat(fixture.teams.home).isNotNull()
        assertThat(fixture.teams.away).isNotNull()
    }

    @Test
    @DisplayName("fixture_1208022.json 파일을 읽어서 fixtureSingle 응답을 반환한다")
    fun `fixture_1208022_json_파일_읽기_테스트`() {
        // Given
        val fixtureId = 1208022L

        // When
        val result = mockFetcher.fetchFixtureSingle(fixtureId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result.get).isEqualTo("fixtures")
        assertThat(result.results).isEqualTo(1)
        assertThat(result.response).hasSize(1)
        
        val fixture = result.response.first()
        assertThat(fixture.fixture).isNotNull()
        assertThat(fixture.fixture.id).isEqualTo(fixtureId)
        assertThat(fixture.league).isNotNull()
        assertThat(fixture.teams).isNotNull()
        assertThat(fixture.teams.home).isNotNull()
        assertThat(fixture.teams.away).isNotNull()
    }

    @Test
    @DisplayName("지원하지 않는 fixture ID에 대해서는 빈 응답을 반환한다")
    fun `지원하지_않는_fixture_id_테스트`() {
        // Given
        val unsupportedFixtureId = 999999L

        // When
        val result = mockFetcher.fetchFixtureSingle(unsupportedFixtureId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result.get).isEqualTo("fixtures")
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }

    @Test
    @DisplayName("teamsOfLeague_leagueId39_season2024.json 파일을 읽어서 teamsOfLeague 응답을 반환한다")
    fun `teamsOfLeague_leagueId39_season2024_json_파일_읽기_테스트`() {
        // Given
        val leagueId = 39L
        val season = 2024

        // When
        val result = mockFetcher.fetchTeamsOfLeague(leagueId, season)

        // Then
        assertThat(result).isNotNull()
        assertThat(result.get).isEqualTo("teams")
        assertThat(result.results).isEqualTo(20)
        assertThat(result.response).hasSize(20)
        
        // 첫 번째 팀 (Manchester United) 검증
        val firstTeam = result.response.first()
        assertThat(firstTeam.team).isNotNull()
        assertThat(firstTeam.team.id).isEqualTo(33)
        assertThat(firstTeam.team.name).isEqualTo("Manchester United")
        assertThat(firstTeam.team.code).isEqualTo("MUN")
        assertThat(firstTeam.team.country).isEqualTo("England")
        assertThat(firstTeam.team.founded).isEqualTo(1878)
        assertThat(firstTeam.team.national).isFalse()
        assertThat(firstTeam.team.logo).isEqualTo("https://media.api-sports.io/football/teams/33.png")
        
        // 첫 번째 팀의 venue 검증
        assertThat(firstTeam.venue).isNotNull()
        assertThat(firstTeam.venue.id).isEqualTo(556)
        assertThat(firstTeam.venue.name).isEqualTo("Old Trafford")
        assertThat(firstTeam.venue.address).isEqualTo("Sir Matt Busby Way")
        assertThat(firstTeam.venue.city).isEqualTo("Manchester")
        assertThat(firstTeam.venue.capacity).isEqualTo(76212)
        assertThat(firstTeam.venue.surface).isEqualTo("grass")
        assertThat(firstTeam.venue.image).isEqualTo("https://media.api-sports.io/football/venues/556.png")
        
        // 마지막 팀 (Aston Villa) 검증
        val lastTeam = result.response.last()
        assertThat(lastTeam.team.id).isEqualTo(66)
        assertThat(lastTeam.team.name).isEqualTo("Aston Villa")
        assertThat(lastTeam.team.code).isEqualTo("AST")
        assertThat(lastTeam.venue.name).isEqualTo("Villa Park")
        assertThat(lastTeam.venue.city).isEqualTo("Birmingham")
    }

    @Test
    @DisplayName("지원하지 않는 league ID에 대해서는 빈 응답을 반환한다")
    fun `지원하지_않는_league_id_테스트`() {
        // Given
        val unsupportedLeagueId = 999L
        val season = 2024

        // When
        val result = mockFetcher.fetchTeamsOfLeague(unsupportedLeagueId, season)

        // Then
        assertThat(result).isNotNull()
        assertThat(result.get).isEqualTo("teams")
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }

    @Test
    @DisplayName("지원하지 않는 season에 대해서는 빈 응답을 반환한다")
    fun `지원하지_않는_season_테스트`() {
        // Given
        val leagueId = 39L
        val unsupportedSeason = 2023

        // When
        val result = mockFetcher.fetchTeamsOfLeague(leagueId, unsupportedSeason)

        // Then
        assertThat(result).isNotNull()
        assertThat(result.get).isEqualTo("teams")
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }
} 