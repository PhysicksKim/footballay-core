package com.footballay.core.infra.apisports.shared.fetch.impl

import com.footballay.core.infra.apisports.shared.fetch.impl.ApiSportsV3MockFetcher
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
        assertThat(teamResponse.team.id).isEqualTo(supportedTeamId)
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
        val season = ApiSportsV3MockFetcher.SUPPORTED_SEASON

        // when
        val result = mockFetcher.fetchFixturesOfLeague(leagueId, season)

        // then
        assertThat(result).isNotNull
        assertThat(result.get).isEqualTo("fixtures")
        assertThat(result.results).isNotEqualTo(0)
        assertThat(result.response).isNotEmpty()
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

    // ------여기 이하에 테스트를 작성해줘-----

    @Test
    fun `fetchFixturesOfLeague는 지원되는 리그와 시즌에 대해 5개의 경기를 반환해야 한다`() {
        // given
        val supportedLeagueId = ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID
        val supportedSeason = ApiSportsV3MockFetcher.SUPPORTED_SEASON

        // when
        val result = mockFetcher.fetchFixturesOfLeague(supportedLeagueId, supportedSeason)

        // then
        assertThat(result).isNotNull
        assertThat(result.get).isEqualTo("fixtures")
        assertThat(result.results).isEqualTo(5)
        assertThat(result.response).hasSize(5)
        
        // 첫 번째 경기 검증 (Manchester United vs Fulham)
        val firstFixture = result.response.first()
        assertThat(firstFixture.fixture.id).isEqualTo(1208021L)
        assertThat(firstFixture.teams.home.name).isEqualTo("Manchester United")
        assertThat(firstFixture.teams.away.name).isEqualTo("Fulham")
        assertThat(firstFixture.goals.home).isEqualTo(1)
        assertThat(firstFixture.goals.away).isEqualTo(0)
        assertThat(firstFixture.league.name).isEqualTo("Premier League")
        assertThat(firstFixture.league.season).isEqualTo(ApiSportsV3MockFetcher.SUPPORTED_SEASON)
    }

    @Test
    fun `fetchFixturesOfLeague는 지원되지 않는 리그에 대해 빈 결과를 반환해야 한다`() {
        // given
        val unsupportedLeagueId = 999L
        val supportedSeason = ApiSportsV3MockFetcher.SUPPORTED_SEASON

        // when
        val result = mockFetcher.fetchFixturesOfLeague(unsupportedLeagueId, supportedSeason)

        // then
        assertThat(result).isNotNull
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }

    @Test
    fun `fetchFixturesOfLeague는 지원되지 않는 시즌에 대해 빈 결과를 반환해야 한다`() {
        // given
        val supportedLeagueId = ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID
        val unsupportedSeason = 1234

        // when
        val result = mockFetcher.fetchFixturesOfLeague(supportedLeagueId, unsupportedSeason)

        // then
        assertThat(result).isNotNull
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }

    @Test
    fun `fetchFixtureSingle은 지원되는 fixture ID에 대해 상세한 경기 정보를 반환해야 한다`() {
        // given
        val supportedFixtureId = 1208397L // Manchester United vs Aston Villa

        // when
        val result = mockFetcher.fetchFixtureSingle(supportedFixtureId)

        // then
        assertThat(result).isNotNull
        assertThat(result.get).isEqualTo("fixtures")
        assertThat(result.results).isEqualTo(1)
        assertThat(result.response).hasSize(1)
        
        val fixture = result.response.first()
        assertThat(fixture.fixture.id).isEqualTo(supportedFixtureId)
        assertThat(fixture.fixture.referee).isEqualTo("T. Bramall")
        assertThat(fixture.fixture.timezone).isEqualTo("Asia/Seoul")
        assertThat(fixture.teams.home.name).isEqualTo("Manchester United")
        assertThat(fixture.teams.away.name).isEqualTo("Aston Villa")
        assertThat(fixture.goals.home).isEqualTo(2)
        assertThat(fixture.goals.away).isEqualTo(0)
        assertThat(fixture.league.name).isEqualTo("Premier League")
        assertThat(fixture.league.round).isEqualTo("Regular Season - 38")
    }

    @Test
    fun `fetchFixtureSingle은 지원되지 않는 fixture ID에 대해 빈 결과를 반환해야 한다`() {
        // given
        val unsupportedFixtureId = 999999L

        // when
        val result = mockFetcher.fetchFixtureSingle(unsupportedFixtureId)

        // then
        assertThat(result).isNotNull
        assertThat(result.results).isEqualTo(0)
        assertThat(result.response).isEmpty()
    }

    @Test
    fun `fetchFixtureSingle은 이벤트 정보를 포함해야 한다`() {
        // given
        val supportedFixtureId = 1208397L

        // when
        val result = mockFetcher.fetchFixtureSingle(supportedFixtureId)

        // then
        val fixture = result.response.first()
        assertThat(fixture.events).isNotNull
        assertThat(fixture.events).hasSize(2)
        
        val firstEvent = fixture.events.first()
        assertThat(firstEvent.type).isEqualTo("subst")
        assertThat(firstEvent.detail).isEqualTo("Substitution 1")
        assertThat(firstEvent.player?.name).isEqualTo("N. Mazraoui")
        assertThat(firstEvent.assist?.name).isEqualTo("Diogo Dalot")
        
        val secondEvent = fixture.events[1]
        assertThat(secondEvent.type).isEqualTo("Goal")
        assertThat(secondEvent.detail).isEqualTo("Normal Goal")
        assertThat(secondEvent.player?.name).isEqualTo("A. Diallo")
    }

    @Test
    fun `fetchFixtureSingle은 라인업 정보를 포함해야 한다`() {
        // given
        val supportedFixtureId = 1208397L

        // when
        val result = mockFetcher.fetchFixtureSingle(supportedFixtureId)

        // then
        val fixture = result.response.first()
        assertThat(fixture.lineups).isNotNull
        assertThat(fixture.lineups).hasSize(1)
        
        val lineup = fixture.lineups.first()
        assertThat(lineup.team.name).isEqualTo("Manchester United")
        assertThat(lineup.formation).isEqualTo("3-4-2-1")
        assertThat(lineup.coach.name).isEqualTo("Ruben Amorim")
        assertThat(lineup.startXI).hasSize(1)
        assertThat(lineup.substitutes).hasSize(1)
        
        val startingPlayer = lineup.startXI.first()
        assertThat(startingPlayer.player.name).isEqualTo("A. Bayındır")
        assertThat(startingPlayer.player.number).isEqualTo(1)
        assertThat(startingPlayer.player.pos).isEqualTo("G")
    }

    @Test
    fun `fetchFixtureSingle은 통계 정보를 포함해야 한다`() {
        // given
        val supportedFixtureId = 1208397L

        // when
        val result = mockFetcher.fetchFixtureSingle(supportedFixtureId)

        // then
        val fixture = result.response.first()
        assertThat(fixture.statistics).isNotNull
        assertThat(fixture.statistics).hasSize(1)
        
        val teamStats = fixture.statistics.first()
        assertThat(teamStats.team.name).isEqualTo("Manchester United")
        assertThat(teamStats.getShotsOffGoal()).isNotNegative()
        assertThat(teamStats.getShotsOnGoal()).isNotNegative()
        assertThat(teamStats.getExpectedGoals()).isNotBlank()
    }

    @Test
    fun `fetchFixtureSingle은 선수 통계 정보를 포함해야 한다`() {
        // given
        val supportedFixtureId = 1208397L

        // when
        val result = mockFetcher.fetchFixtureSingle(supportedFixtureId)

        // then
        val fixture = result.response.first()
        assertThat(fixture.players).isNotNull
        assertThat(fixture.players).hasSize(1)
        
        val teamPlayers = fixture.players.first()
        assertThat(teamPlayers.team.name).isEqualTo("Manchester United")
        assertThat(teamPlayers.players).hasSize(1)
        
        val player = teamPlayers.players.first()
        assertThat(player.player.name).isEqualTo("Altay Bayındır")
        assertThat(player.player.photo).isEqualTo("https://media.api-sports.io/football/players/50132.png")
        
        val playerStats = player.statistics.first()
        assertThat(playerStats.games.minutes).isEqualTo(90)
        assertThat(playerStats.games.number).isEqualTo(1)
        assertThat(playerStats.games.position).isEqualTo("G")
        assertThat(playerStats.games.rating).isEqualTo("7")
        assertThat(playerStats.games.captain).isFalse()
        assertThat(playerStats.games.substitute).isFalse()
    }

    @Test
    fun `모든 지원되는 fixture ID들이 올바르게 반환되어야 한다`() {
        // given
        val supportedFixtureIds = listOf(1208021L, 1208022L, 1208025L, 1208028L, 1208397L)

        // when & then
        supportedFixtureIds.forEach { fixtureId ->
            val result = mockFetcher.fetchFixtureSingle(fixtureId)
            assertThat(result.results).isEqualTo(1)
            assertThat(result.response).hasSize(1)
            assertThat(result.response.first().fixture.id).isEqualTo(fixtureId)
        }
    }

    @Test
    fun `fetchFixturesOfLeague의 모든 경기가 올바른 구조를 가져야 한다`() {
        // given
        val supportedLeagueId = ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID
        val supportedSeason = ApiSportsV3MockFetcher.SUPPORTED_SEASON

        // when
        val result = mockFetcher.fetchFixturesOfLeague(supportedLeagueId, supportedSeason)

        // then
        result.response.forEach { fixture ->
            assertThat(fixture.fixture.id).isNotNull()
            assertThat(fixture.fixture.referee).isNotNull()
            assertThat(fixture.fixture.date).isNotNull()
            assertThat(fixture.fixture.timestamp).isNotNull()
            assertThat(fixture.teams.home.id).isNotNull()
            assertThat(fixture.teams.home.name).isNotNull()
            assertThat(fixture.teams.away.id).isNotNull()
            assertThat(fixture.teams.away.name).isNotNull()
            assertThat(fixture.goals.home).isNotNull()
            assertThat(fixture.goals.away).isNotNull()
            assertThat(fixture.league.id).isEqualTo(ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID)
            assertThat(fixture.league.name).isEqualTo("Premier League")
            assertThat(fixture.league.season).isEqualTo(ApiSportsV3MockFetcher.SUPPORTED_SEASON)
        }
    }

} 