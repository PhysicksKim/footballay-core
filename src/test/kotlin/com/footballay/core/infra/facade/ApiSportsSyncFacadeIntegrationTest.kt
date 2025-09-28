package com.footballay.core.infra.facade

import com.footballay.core.infra.apisports.shared.fetch.impl.ApiSportsV3MockFetcher
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.logger
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("dev", "mockapi")
@Transactional
class ApiSportsSyncFacadeIntegrationTest {

    val log = logger()

    @Autowired
    lateinit var apiSportsBackboneSyncFacadeImpl: ApiSportsBackboneSyncFacadeImpl

    @Autowired
    lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    lateinit var teamApiSportsRepository: TeamApiSportsRepository

    @Autowired
    lateinit var playerApiSportsRepository: PlayerApiSportsRepository

    @Autowired
    lateinit var fixtureApiSportsRepository: FixtureApiSportsRepository

    @Autowired
    lateinit var em: EntityManager

    companion object {
        private val SUPPORTED_LEAGUE_API_ID = ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID
        private val SUPPORTED_SEASON = ApiSportsV3MockFetcher.SUPPORTED_SEASON
        private val SUPPORTED_TEAM_API_ID = ApiSportsV3MockFetcher.SUPPORTED_TEAM_ID
    }

    @Test
    fun `syncCurrentLeagues는 목 페처에서 리그들을 동기화해야 한다`() {
        // when
        val syncedCount = apiSportsBackboneSyncFacadeImpl.syncCurrentLeagues()

        // then
        assertThat(syncedCount).isEqualTo(2) // Premier League + La Liga

        // DB에서 조회하여 검증
        val savedLeagues = leagueApiSportsRepository.findAll()
        assertThat(savedLeagues).hasSize(2)

        val premierLeague = savedLeagues.find { it.apiId == SUPPORTED_LEAGUE_API_ID }
        assertThat(premierLeague).isNotNull
        assertThat(premierLeague!!.name).isEqualTo("Premier League")
        assertThat(premierLeague.countryName).isEqualTo("England")
        assertThat(premierLeague.currentSeason).isEqualTo(SUPPORTED_SEASON)
    }

    @Test
    fun `syncPlayersOfTeam은 지원되지 않는 팀에 대해 0을 반환해야 한다`() {
        // given
        val unsupportedTeamApiId = 999L

        // when
        val syncedCount = apiSportsBackboneSyncFacadeImpl.syncPlayersOfTeam(unsupportedTeamApiId)

        // then
        assertThat(syncedCount).isEqualTo(0)
    }

    @Test
    fun `완전한 통합 테스트 - 전체 동기화 워크플로우`() {
        // when & then - 순차적으로 동기화
        // 1. 리그 동기화
        val leagueCount = apiSportsBackboneSyncFacadeImpl.syncCurrentLeagues()
        assertThat(leagueCount).isEqualTo(2)

        // 2. 팀 동기화
        val teamCount = apiSportsBackboneSyncFacadeImpl.syncTeamsOfLeagueWithCurrentSeason(SUPPORTED_LEAGUE_API_ID)
        assertThat(teamCount).isGreaterThan(0)

        // 3. 선수 동기화
        val playerCount = apiSportsBackboneSyncFacadeImpl.syncPlayersOfTeam(SUPPORTED_TEAM_API_ID)
        assertThat(playerCount).isGreaterThan(0)

        // 최종 검증
        val finalLeagues = leagueApiSportsRepository.findAll()
        val finalTeams = teamApiSportsRepository.findAll()
        val finalPlayers = playerApiSportsRepository.findAll()

        assertThat(finalLeagues).hasSize(2)
        assertThat(finalTeams).isNotEmpty
        assertThat(finalPlayers).isNotEmpty

        // 특정 데이터 검증
        val premierLeague = finalLeagues.find { it.apiId == SUPPORTED_LEAGUE_API_ID }
        val manchesterCity = finalTeams.find { it.apiId == SUPPORTED_TEAM_API_ID }
        val haalandPlayer = finalPlayers.find { it.name == "Erling Haaland" }

        assertThat(premierLeague).isNotNull
        assertThat(manchesterCity).isNotNull
        assertThat(haalandPlayer).isNotNull

        // apiId 검증 (core id와 구분)
        assertThat(premierLeague!!.apiId).isEqualTo(SUPPORTED_LEAGUE_API_ID)
        assertThat(manchesterCity!!.apiId).isEqualTo(SUPPORTED_TEAM_API_ID)
        assertThat(haalandPlayer!!.apiId).isNotNull
        assertThat(haalandPlayer.position).isEqualTo("Attacker")
        assertThat(haalandPlayer.age).isEqualTo(23)
    }

    @Test
    fun `syncFixturesOfLeagueWithSeason은 리그의 시즌 경기들을 동기화해야 한다`() {
        // given
        val leagueApiId = SUPPORTED_LEAGUE_API_ID
        val season = SUPPORTED_SEASON
        apiSportsBackboneSyncFacadeImpl.syncCurrentLeagues()
        apiSportsBackboneSyncFacadeImpl.syncTeamsOfLeagueWithCurrentSeason(leagueApiId)

        // when
        val syncedCount = apiSportsBackboneSyncFacadeImpl.syncFixturesOfLeagueWithSeason(leagueApiId, season)
        em.flush()
        em.clear()

        // then
        assertThat(syncedCount).isEqualTo(5) // Mock fetcher에서 제공하는 5개의 경기

        // DB에서 조회하여 검증
        val savedFixtures = fixtureApiSportsRepository.findAll()
        assertThat(savedFixtures).hasSize(5)

        // 첫 번째 경기 검증 (Manchester United vs Fulham)
        val firstFixture = savedFixtures.find { it.apiId == 1208021L }
        assertThat(firstFixture).isNotNull
        assertThat(firstFixture!!.referee).isEqualTo("Mock Referee")
        assertThat(firstFixture.timezone).isEqualTo("UTC")
        assertThat(firstFixture.round).isEqualTo("Regular Season - 1")

        // 홈팀 검증
        assertThat(firstFixture.core?.homeTeam?.teamApiSports?.apiId).isEqualTo(33L)
        assertThat(firstFixture.core?.homeTeam?.teamApiSports?.name).isEqualTo("Manchester United")
        assertThat(firstFixture.core?.homeTeam?.teamApiSports?.logo).isEqualTo("https://media.api-sports.io/football/teams/33.png")

        // 원정팀 검증
        assertThat(firstFixture.core?.awayTeam?.teamApiSports?.apiId).isEqualTo(36L)
        assertThat(firstFixture.core?.awayTeam?.teamApiSports?.name).isEqualTo("Fulham")
        assertThat(firstFixture.core?.awayTeam?.teamApiSports?.logo).isEqualTo("https://media.api-sports.io/football/teams/36.png")

        // 스코어 검증
        assertThat(firstFixture.score?.halftimeHome).isEqualTo(1)
        assertThat(firstFixture.score?.halftimeAway).isEqualTo(0)
        assertThat(firstFixture.score?.fulltimeHome).isEqualTo(1)
        assertThat(firstFixture.score?.fulltimeAway).isEqualTo(0)
    }

    @Test
    fun `syncFixturesOfLeagueWithSeason은 지원되지 않는 시즌에 예외를 반환해야 한다`() {
        // given
        val leagueApiId = SUPPORTED_LEAGUE_API_ID
        val supportSeason = SUPPORTED_SEASON
        val unsupportedSeason = 1234
        apiSportsBackboneSyncFacadeImpl.syncCurrentLeagues()
        apiSportsBackboneSyncFacadeImpl.syncTeamsOfLeague(leagueApiId, supportSeason)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            apiSportsBackboneSyncFacadeImpl.syncFixturesOfLeagueWithSeason(leagueApiId, unsupportedSeason)
        }

        // DB에 저장된 fixture가 없어야 함
        val savedFixtures = fixtureApiSportsRepository.findAll()
        assertThat(savedFixtures).isEmpty()
    }

    @Test
    fun `완전한 fixture 동기화 워크플로우 - 리그, 팀, 경기 순차 동기화`() {
        // when & then - 순차적으로 동기화
        // 1. 리그 동기화
        val leagueCount = apiSportsBackboneSyncFacadeImpl.syncCurrentLeagues()
        assertThat(leagueCount).isEqualTo(2)

        // 2. 팀 동기화
        val teamCount = apiSportsBackboneSyncFacadeImpl.syncTeamsOfLeagueWithCurrentSeason(SUPPORTED_LEAGUE_API_ID)
        assertThat(teamCount).isGreaterThan(0)

        // 3. 경기 동기화
        val fixtureCount = apiSportsBackboneSyncFacadeImpl.syncFixturesOfLeagueWithSeason(SUPPORTED_LEAGUE_API_ID, SUPPORTED_SEASON)
        assertThat(fixtureCount).isEqualTo(5)

        em.flush()
        em.clear()

        // 최종 검증
        val finalLeagues = leagueApiSportsRepository.findAll()
        val finalTeams = teamApiSportsRepository.findAll()
        val finalFixtures = fixtureApiSportsRepository.findAll()

        assertThat(finalLeagues).hasSize(2)
        assertThat(finalTeams).isNotEmpty
        assertThat(finalFixtures).hasSize(5)

        // 특정 데이터 검증
        val premierLeague = finalLeagues.find { it.apiId == SUPPORTED_LEAGUE_API_ID }
        val manchesterUnited = finalTeams.find { it.apiId == 33L }
        val manchesterUnitedFixture = finalFixtures.find { it.core?.homeTeam?.teamApiSports?.apiId == 33L }

        assertThat(premierLeague).isNotNull
        assertThat(manchesterUnited).isNotNull
        assertThat(manchesterUnitedFixture).isNotNull

        // fixture 상세 검증
        assertThat(manchesterUnitedFixture!!.apiId).isEqualTo(1208021L)
        assertThat(manchesterUnitedFixture.core?.homeTeam?.teamApiSports?.name).isEqualTo("Manchester United")
        assertThat(manchesterUnitedFixture.core?.awayTeam?.teamApiSports?.name).isEqualTo("Fulham")
        assertThat(manchesterUnitedFixture.season?.leagueApiSports?.apiId).isEqualTo(SUPPORTED_LEAGUE_API_ID)
        assertThat(manchesterUnitedFixture.season?.seasonYear).isEqualTo(SUPPORTED_SEASON)
    }

    @Test
    fun `syncFixturesOfLeagueWithSeason은 모든 경기의 상세 정보를 올바르게 저장해야 한다`() {
        // given
        val leagueApiId = SUPPORTED_LEAGUE_API_ID
        val season = SUPPORTED_SEASON
        apiSportsBackboneSyncFacadeImpl.syncCurrentLeagues()
        apiSportsBackboneSyncFacadeImpl.syncTeamsOfLeagueWithCurrentSeason(leagueApiId)

        // when
        val syncedCount = apiSportsBackboneSyncFacadeImpl.syncFixturesOfLeagueWithSeason(leagueApiId, season)
        em.flush()
        em.clear()

        // then
        assertThat(syncedCount).isEqualTo(5)

        val savedFixtures = fixtureApiSportsRepository.findAll()
        assertThat(savedFixtures).hasSize(5)

        log.info("saved fixtures api id list : ${savedFixtures.map { it.apiId }}")

        // 모든 경기의 기본 구조 검증
        savedFixtures.forEach { fixture ->
            assertThat(fixture.apiId).isNotNull()
            assertThat(fixture.referee).isNotNull()
            assertThat(fixture.timezone).isNotNull()
            assertThat(fixture.date).isNotNull()
            assertThat(fixture.timestamp).isNotNull()
            assertThat(fixture.round).isNotNull()
            assertThat(fixture.core).isNotNull()
            assertThat(fixture.core?.homeTeam).isNotNull()
            assertThat(fixture.core?.awayTeam).isNotNull()
            assertThat(fixture.season?.leagueApiSports?.apiId).isEqualTo(leagueApiId)
            assertThat(fixture.season?.seasonYear).isEqualTo(season)
        }

        // 특정 경기들의 상세 검증
        val manUtdVsFulham = savedFixtures.find { it.apiId == 1208021L }
        val arsenalVsWolves = savedFixtures.find { it.apiId == 1208028L }
        val manUtdVsAstonVilla = savedFixtures.find { it.apiId == 1208397L }

        log.info("expect core home not null : ${manUtdVsFulham?.core?.homeTeam}")
        log.info("expect core away not null : ${manUtdVsFulham?.core?.awayTeam}")
        log.info("expect manutd : ${manUtdVsFulham?.core?.homeTeam?.teamApiSports}")
        log.info("expect fulham : ${manUtdVsFulham?.core?.awayTeam?.teamApiSports}")

        // Manchester United vs Fulham 검증
        assertThat(manUtdVsFulham).isNotNull
        assertThat(manUtdVsFulham?.core?.homeTeam?.teamApiSports?.name).isEqualTo("Manchester United")
        assertThat(manUtdVsFulham?.core?.awayTeam?.teamApiSports?.name).isEqualTo("Fulham")
        assertThat(manUtdVsFulham?.score?.fulltimeHome).isEqualTo(1)
        assertThat(manUtdVsFulham?.score?.fulltimeAway).isEqualTo(0)

        // Arsenal vs Wolves 검증
        assertThat(arsenalVsWolves).isNotNull
        assertThat(arsenalVsWolves?.core?.homeTeam?.teamApiSports?.name).isEqualTo("Arsenal")
        assertThat(arsenalVsWolves?.core?.awayTeam?.teamApiSports?.name).isEqualTo("Wolves")
        assertThat(arsenalVsWolves?.score?.fulltimeHome).isEqualTo(3)
        assertThat(arsenalVsWolves?.score?.fulltimeAway).isEqualTo(0)

        // Manchester United vs Aston Villa 검증 (마지막 경기)
        assertThat(manUtdVsAstonVilla).isNotNull
        assertThat(manUtdVsAstonVilla?.core?.homeTeam?.teamApiSports?.name).isEqualTo("Manchester United")
        assertThat(manUtdVsAstonVilla?.core?.awayTeam?.teamApiSports?.name).isEqualTo("Aston Villa")
        assertThat(manUtdVsAstonVilla?.score?.fulltimeHome).isEqualTo(2)
        assertThat(manUtdVsAstonVilla?.score?.fulltimeAway).isEqualTo(0)
        assertThat(manUtdVsAstonVilla?.round).isEqualTo("Regular Season - 38")
    }
}