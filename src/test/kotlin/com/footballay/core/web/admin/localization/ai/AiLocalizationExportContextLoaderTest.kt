package com.footballay.core.web.admin.localization.ai

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

/** AI export의 League-Team-Player 문맥 탐색을 확인합니다. */
@ExtendWith(MockitoExtension::class)
class AiLocalizationExportContextLoaderTest {
    @Mock
    private lateinit var leagueFacade: LeagueFacade

    @Test
    @DisplayName("Player 요청의 League, Team, Player 문맥을 조립한다")
    fun loadPlayers_assemblesRequestedContext() {
        val league = LeagueModel("league-1", "Premier League", null, true)
        val team = TeamModel("team-1", "Arsenal", "ARS")
        val player = PlayerModel("player-1", "Bukayo Saka", null, null, null)
        whenever(leagueFacade.findLeagueByUid("league-1")).thenReturn(DomainResult.Success(league))
        whenever(leagueFacade.findTeamsByLeagueUid("league-1")).thenReturn(DomainResult.Success(listOf(team)))
        whenever(leagueFacade.findTeamByUid("team-1")).thenReturn(DomainResult.Success(team))
        whenever(leagueFacade.findPlayersByTeamUid("team-1")).thenReturn(DomainResult.Success(listOf(player)))

        val result = loader().loadPlayers("league-1", "team-1", listOf("player-1"))

        assertThat((result as DomainResult.Success).value).isEqualTo(PlayerExportContext(league, team, listOf(player)))
    }

    @Test
    @DisplayName("Team 요청의 League와 Team 문맥을 조립한다")
    fun loadTeams_assemblesRequestedContext() {
        val league = LeagueModel("league-1", "Premier League", null, true)
        val team = TeamModel("team-1", "Arsenal", "ARS")
        whenever(leagueFacade.findLeagueByUid("league-1")).thenReturn(DomainResult.Success(league))
        whenever(leagueFacade.findTeamsByLeagueUid("league-1")).thenReturn(DomainResult.Success(listOf(team)))

        val result = loader().loadTeams("league-1", listOf("team-1"))

        assertThat((result as DomainResult.Success).value).isEqualTo(TeamExportContext(league, listOf(team)))
    }

    @Test
    @DisplayName("요청 UID가 League-Team-Player 문맥 밖이면 거부한다")
    fun loader_rejectsUidsOutsideHierarchy() {
        val league = LeagueModel("league-1", "Premier League", null, true)
        val team = TeamModel("team-1", "Arsenal", "ARS")
        whenever(leagueFacade.findLeagueByUid("league-1")).thenReturn(DomainResult.Success(league))
        whenever(leagueFacade.findTeamsByLeagueUid("league-1")).thenReturn(DomainResult.Success(listOf(team)))
        whenever(leagueFacade.findTeamByUid("team-1")).thenReturn(DomainResult.Success(team))
        whenever(leagueFacade.findPlayersByTeamUid("team-1")).thenReturn(
            DomainResult.Success(listOf(PlayerModel("player-1", "Bukayo Saka", null, null, null))),
        )

        val teamResult = loader().loadTeams("league-1", listOf("team-2"))
        val playerTeamResult = loader().loadPlayers("league-1", "team-2", listOf("player-1"))
        val playerResult = loader().loadPlayers("league-1", "team-1", listOf("player-2"))

        assertThat((teamResult as DomainResult.Fail).error.validationErrorCode()).isEqualTo("TEAM_NOT_IN_LEAGUE")
        assertThat((playerTeamResult as DomainResult.Fail).error.validationErrorCode()).isEqualTo("TEAM_NOT_IN_LEAGUE")
        assertThat((playerResult as DomainResult.Fail).error.validationErrorCode()).isEqualTo("PLAYER_NOT_IN_TEAM")
    }

    private fun loader() = AiLocalizationExportContextLoader(leagueFacade)

    private fun DomainFail.validationErrorCode() = (this as DomainFail.Validation).errors.single().code
}
