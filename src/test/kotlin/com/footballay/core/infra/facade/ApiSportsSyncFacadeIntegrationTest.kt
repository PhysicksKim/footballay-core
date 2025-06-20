package com.footballay.core.infra.facade

import com.footballay.core.infra.apisports.LeagueApiSportsQueryService
import com.footballay.core.infra.apisports.fetch.impl.ApiSportsV3MockFetcher
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
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

    @Autowired
    lateinit var apiSportsSyncFacade: ApiSportsSyncFacade

    @Autowired
    lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    lateinit var teamApiSportsRepository: TeamApiSportsRepository

    @Autowired
    lateinit var playerApiSportsRepository: PlayerApiSportsRepository

    companion object {
        private val SUPPORTED_LEAGUE_API_ID = ApiSportsV3MockFetcher.SUPPORTED_LEAGUE_ID
        private val SUPPORTED_SEASON = ApiSportsV3MockFetcher.SUPPORTED_SEASON
        private val SUPPORTED_TEAM_API_ID = ApiSportsV3MockFetcher.SUPPORTED_TEAM_ID
    }

    @Test
    fun `syncCurrentLeagues는 목 페처에서 리그들을 동기화해야 한다`() {
        // when
        val syncedCount = apiSportsSyncFacade.syncCurrentLeagues()

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
        val syncedCount = apiSportsSyncFacade.syncPlayersOfTeam(unsupportedTeamApiId)

        // then
        assertThat(syncedCount).isEqualTo(0)
    }

    @Test
    fun `완전한 통합 테스트 - 전체 동기화 워크플로우`() {
        // when & then - 순차적으로 동기화
        // 1. 리그 동기화
        val leagueCount = apiSportsSyncFacade.syncCurrentLeagues()
        assertThat(leagueCount).isEqualTo(2)

        // 2. 팀 동기화
        val teamCount = apiSportsSyncFacade.syncTeamsOfLeagueWithCurrentSeason(SUPPORTED_LEAGUE_API_ID)
        assertThat(teamCount).isGreaterThan(0)

        // 3. 선수 동기화
        val playerCount = apiSportsSyncFacade.syncPlayersOfTeam(SUPPORTED_TEAM_API_ID)
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
}