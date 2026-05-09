package com.footballay.core.web.admin.apisports.service

import com.footballay.core.admin.apisports.query.AdminApiSportsQueryFacade
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
        return teams.map { view ->
            TeamApiSportsAdminResponse(
                apiId = view.apiId,
                uid = view.uid,
                name = view.name,
                nameKo = view.nameKo,
                logo = view.logo,
                code = view.code,
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
        return players.map { view ->
            PlayerApiSportsAdminResponse(
                apiId = view.apiId,
                uid = view.uid,
                name = view.name,
                nameKo = view.nameKo,
                photo = view.photo,
                position = view.position,
                number = view.number,
                nationality = view.nationality,
            )
        }
    }
}
