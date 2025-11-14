package com.footballay.core.web.admin.apisports.service

import com.footballay.core.domain.admin.apisports.facade.AdminApiSportsQueryFacade
import com.footballay.core.web.admin.apisports.dto.PlayerAdminResponse
import com.footballay.core.web.admin.apisports.dto.TeamAdminResponse
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
    fun findTeamsByLeagueApiId(leagueApiId: Long): List<TeamAdminResponse> {
        val result = adminApiSportsQueryFacade.findTeamsByLeagueApiId(leagueApiId)

        // DomainResult에서 Model 추출 (현재는 항상 Success)
        val teams = result.getOrNull() ?: emptyList()

        // Domain Model → Web Response DTO 변환
        return teams.map { model ->
            TeamAdminResponse(
                teamApiId = model.teamApiId,
                teamCoreId = model.teamCoreId,
                uid = model.uid,
                name = model.name,
                code = model.code,
                country = model.country,
                details = model.details,
                detailsType = model.detailsType,
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
    fun findPlayersByTeamApiId(teamApiId: Long): List<PlayerAdminResponse> {
        val result = adminApiSportsQueryFacade.findPlayersByTeamApiId(teamApiId)

        // DomainResult에서 Model 추출 (현재는 항상 Success)
        val players = result.getOrNull() ?: emptyList()

        // Domain Model → Web Response DTO 변환
        return players.map { model ->
            PlayerAdminResponse(
                playerApiId = model.playerApiId,
                playerCoreId = model.playerCoreId,
                uid = model.uid,
                name = model.name,
                firstname = model.firstname,
                lastname = model.lastname,
                position = model.position,
                number = model.number,
                details = model.details,
                detailsType = model.detailsType,
            )
        }
    }
}
