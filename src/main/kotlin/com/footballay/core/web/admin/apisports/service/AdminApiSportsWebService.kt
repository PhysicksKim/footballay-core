package com.footballay.core.web.admin.apisports.service

import com.footballay.core.infra.facade.ApiSportsSyncFacade
import com.footballay.core.web.admin.apisports.dto.LeaguesSyncResultDto
import com.footballay.core.web.admin.apisports.dto.PlayersSyncResultDto
import com.footballay.core.web.admin.apisports.dto.TeamsSyncResultDto
import com.footballay.core.web.common.dto.ApiResponseV2
import com.footballay.core.web.common.dto.ErrorDetail
import com.footballay.core.logger
import org.apache.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class AdminApiSportsWebService(
    private val apiSportsSyncFacade: ApiSportsSyncFacade
) {
    
    val log = logger()

    /**
     * 현재 시즌의 모든 리그를 동기화합니다.
     */
    fun syncCurrentLeagues(): ApiResponseV2<LeaguesSyncResultDto> {
        return try {
            log.info("Starting current leagues sync request")
            
            val syncedCount = apiSportsSyncFacade.syncCurrentLeagues()
            
            log.info("Successfully synced $syncedCount leagues")
            val result = LeaguesSyncResultDto(
                syncedCount = syncedCount,
                message = "현재 시즌 리그 $syncedCount 개가 동기화되었습니다"
            )
            ApiResponseV2.success(result)
        } catch (e: Exception) {
            log.error("Failed to sync current leagues", e)
            ApiResponseV2.failure(
                ErrorDetail(message = "리그 동기화 중 오류가 발생했습니다: ${e.message}"),
                code = HttpStatus.SC_INTERNAL_SERVER_ERROR
            )
        }
    }

    /**
     * 특정 리그의 팀들을 동기화합니다.
     * 
     * @param leagueApiId 리그의 ApiSports ID
     * @param season 시즌 연도 (선택사항, 없으면 현재 시즌 사용)
     */
    fun syncTeamsOfLeague(leagueApiId: Long, season: Int?): ApiResponseV2<TeamsSyncResultDto> {
        log.info("Starting teams sync request for leagueApiId=$leagueApiId, season=$season")
        
        // 유효성 검사
        if (leagueApiId <= 0) {
            return ApiResponseV2.failure(ErrorDetail(
                message = "리그 ID는 양수여야 합니다",
                field = "leagueApiId"),
                code = HttpStatus.SC_BAD_REQUEST
            )

        }
        
        if (season != null && (season < 2000 || season > 2050)) {
            return ApiResponseV2.failure(ErrorDetail(
                message = "시즌은 2000-2050 범위여야 합니다",
                field = "season"),
                code = HttpStatus.SC_BAD_REQUEST
            )
        }
        
        return try {
            val syncedCount = if (season != null) {
                // 특정 시즌 지정
                apiSportsSyncFacade.syncTeamsOfLeague(leagueApiId, season)
            } else {
                // 현재 시즌 사용
                apiSportsSyncFacade.syncTeamsOfLeagueWithCurrentSeason(leagueApiId)
            }
            
            log.info("Successfully synced $syncedCount teams for league $leagueApiId")
            val result = TeamsSyncResultDto(
                syncedCount = syncedCount,
                leagueApiId = leagueApiId,
                season = season,
                message = "리그(${leagueApiId})의 팀 $syncedCount 개가 동기화되었습니다"
            )
            ApiResponseV2.success(result)
        } catch (e: Exception) {
            log.error("Failed to sync teams for league $leagueApiId", e)
            ApiResponseV2.failure(ErrorDetail(
                message = "팀 동기화 중 오류가 발생했습니다: ${e.message}"),
                code = HttpStatus.SC_INTERNAL_SERVER_ERROR
            )
        }
    }

    /**
     * 특정 팀의 선수들을 동기화합니다.
     * 
     * @param teamApiId 팀의 ApiSports ID
     */
    fun syncPlayersOfTeam(teamApiId: Long): ApiResponseV2<PlayersSyncResultDto> {
        log.info("Starting players sync request for teamApiId=$teamApiId")
        
        // 유효성 검사
        if (teamApiId <= 0) {
            return ApiResponseV2.failure(ErrorDetail(
                message = "팀 ID는 양수여야 합니다",
                field = "teamApiId"),
                code = HttpStatus.SC_BAD_REQUEST
            )
        }
        
        return try {
            val syncedCount = apiSportsSyncFacade.syncPlayersOfTeam(teamApiId)
            
            log.info("Successfully synced $syncedCount players for team $teamApiId")
            val result = PlayersSyncResultDto(
                syncedCount = syncedCount,
                teamApiId = teamApiId,
                message = if (syncedCount > 0) {
                    "팀(${teamApiId})의 선수 $syncedCount 명이 동기화되었습니다"
                } else {
                    "팀(${teamApiId})에 대한 선수 정보를 찾을 수 없습니다"
                }
            )
            ApiResponseV2.success(result)
        } catch (e: Exception) {
            log.error("Failed to sync players for team $teamApiId", e)
            ApiResponseV2.failure(ErrorDetail(
                message = "선수 동기화 중 오류가 발생했습니다: ${e.message}"),
                code = HttpStatus.SC_INTERNAL_SERVER_ERROR
            )
        }
    }
} 