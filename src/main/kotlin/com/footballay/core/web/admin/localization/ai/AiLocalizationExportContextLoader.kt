package com.footballay.core.web.admin.localization.ai

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamModel
import org.springframework.stereotype.Component

data class TeamExportContext(val league: LeagueModel, val teams: List<TeamModel>)
data class PlayerExportContext(val league: LeagueModel, val team: TeamModel, val players: List<PlayerModel>)

/** AI export에 필요한 Core uid들이 존재하는지 확인하고, 필요한 정보를 제공합니다. */
@Component
class AiLocalizationExportContextLoader(private val leagueFacade: LeagueFacade) {
    fun loadTeams(leagueUid: String, requestedUids: List<String>): DomainResult<TeamExportContext, DomainFail> {
        val league = when (val result = leagueFacade.findLeagueByUid(leagueUid)) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> return result
        }
        val teams = when (val result = leagueFacade.findTeamsByLeagueUid(leagueUid)) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> return result
        }
        val teamsByUid = teams.associateBy { it.uid }
        val missingUids = requestedUids.filterNot(teamsByUid::containsKey)
        if (missingUids.isNotEmpty()) return contextMismatch("TEAM_NOT_IN_LEAGUE", missingUids)
        return DomainResult.Success(TeamExportContext(league, requestedUids.map(teamsByUid::getValue)))
    }

    fun loadPlayers(leagueUid: String, teamUid: String, requestedUids: List<String>): DomainResult<PlayerExportContext, DomainFail> {
        val league = when (val result = leagueFacade.findLeagueByUid(leagueUid)) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> return result
        }
        val leagueTeams = when (val result = leagueFacade.findTeamsByLeagueUid(leagueUid)) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> return result
        }
        if (leagueTeams.none { it.uid == teamUid }) return contextMismatch("TEAM_NOT_IN_LEAGUE", listOf(teamUid))
        val team = when (val result = leagueFacade.findTeamByUid(teamUid)) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> return result
        }
        val players = when (val result = leagueFacade.findPlayersByTeamUid(teamUid)) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> return result
        }
        val playersByUid = players.associateBy { it.uid }
        val missingUids = requestedUids.filterNot(playersByUid::containsKey)
        if (missingUids.isNotEmpty()) return contextMismatch("PLAYER_NOT_IN_TEAM", missingUids)
        return DomainResult.Success(PlayerExportContext(league, team, requestedUids.map(playersByUid::getValue)))
    }

    private fun contextMismatch(code: String, uids: List<String>): DomainResult.Fail<DomainFail.Validation> =
        DomainResult.Fail(DomainFail.Validation(uids.map { DomainFail.Validation.ValidationError(code, "현재 탐색 문맥에 포함되지 않은 UID입니다: $it", "uids") }))
}
