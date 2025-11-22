package com.footballay.core.web.admin.apisports.service

import com.footballay.core.domain.admin.apisports.facade.AdminApiSportsQueryFacade
import com.footballay.core.domain.model.PlayerApiSportsExtension
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamApiSportsExtension
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.web.admin.apisports.dto.PlayerApiSportsAdminResponse
import com.footballay.core.web.admin.apisports.dto.TeamApiSportsAdminResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

/**
 * Admin API - ApiSports 조회 웹 서비스
 *
 * Domain Facade를 호출하여 Domain Model을 받아온 후,
 * 웹 응답 DTO로 변환하는 책임을 가집니다.
 *
 * Controller → WebService → Domain Facade → Repository 계층 구조를 따릅니다.
 */
@Service
class AdminApiSportsQueryWebService(
    private val adminApiSportsQueryFacade: AdminApiSportsQueryFacade,
) {
    /**
     * LeagueApiSports apiId로 해당 리그의 팀 목록 조회
     *
     * @param leagueApiId LeagueApiSports의 apiId (예: 39 = Premier League)
     * @return TeamAdminResponse 목록
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun findTeamsByLeagueApiId(leagueApiId: Long): List<TeamApiSportsAdminResponse> {
        val result = adminApiSportsQueryFacade.findTeamsByLeagueApiId(leagueApiId)

        val teams = result.getOrNull() ?: emptyList()
        val pairList: List<Pair<TeamModel, TeamApiSportsExtension>> =
            teams.mapNotNull { model ->
                val ext = model.extension as? TeamApiSportsExtension ?: return@mapNotNull null
                Pair(model, ext)
            }

        return pairList.map { (model, ext) ->
            TeamApiSportsAdminResponse(
                apiId = ext.apiId,
                uid = model.uid,
                name = model.name,
                nameKo = model.nameKo,
                logo = ext.logo,
                code = model.code,
            )
        }
    }

    /**
     * TeamApiSports apiId로 해당 팀의 선수 목록 조회
     *
     * @param teamApiId TeamApiSports의 apiId (예: 50 = Manchester City)
     * @return PlayerAdminResponse 목록
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun findPlayersByTeamApiId(teamApiId: Long): List<PlayerApiSportsAdminResponse> {
        val result = adminApiSportsQueryFacade.findPlayersByTeamApiId(teamApiId)

        val players = result.getOrNull() ?: emptyList()
        val pairList: List<Pair<PlayerModel, PlayerApiSportsExtension>> =
            players.mapNotNull { model ->
                val ext = model.extension as? PlayerApiSportsExtension ?: return@mapNotNull null
                Pair(model, ext)
            }

        return pairList.map { (model, ext) ->
            PlayerApiSportsAdminResponse(
                apiId = ext.apiId,
                uid = model.uid,
                name = model.name,
                nameKo = model.nameKo,
                photo = model.photo,
                position = model.position,
                number = model.number,
                nationality = ext.nationality,
            )
        }
    }
}
