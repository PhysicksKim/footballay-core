package com.footballay.core.web.admin.apisports.controller

import com.footballay.core.web.admin.apisports.dto.LeaguesSyncResultDto
import com.footballay.core.web.admin.apisports.dto.PlayersSyncResultDto
import com.footballay.core.web.admin.apisports.dto.TeamsSyncResultDto
import com.footballay.core.web.admin.apisports.service.AdminApiSportsWebService
import com.footballay.core.web.common.dto.ApiResponseV2
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/apisports")
class AdminApiSportsController(
    private val adminApiSportsWebService: AdminApiSportsWebService
) {

    /**
     * Controller 헬스 테스트용 엔드포인트
     *
     * GET /api/v1/admin/apisports/test
     */
    @GetMapping("/test")
    fun testAdminController(): ResponseEntity<ApiResponseV2<String>> {
        val response = ApiResponseV2.success("Admin API Sports Controller is working!")
        return ResponseEntity.ok(response)
    }

    /**
     * 현재 시즌의 모든 리그를 동기화합니다.
     * 
     * POST /api/v1/admin/apisports/leagues/sync
     */
    @PostMapping("/leagues/sync")
    fun syncCurrentLeagues(): ResponseEntity<ApiResponseV2<LeaguesSyncResultDto>> {
        val result = adminApiSportsWebService.syncCurrentLeagues()
        
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result)
        }
    }

    /**
     * 특정 리그의 팀들을 현재 시즌으로 동기화합니다.
     * 
     * POST /api/v1/admin/apisports/leagues/{leagueId}/teams/sync
     * 
     * @param leagueId ApiSports 리그 ID
     */
    @PostMapping("/leagues/{leagueId}/teams/sync")
    fun syncTeamsOfLeagueWithCurrentSeason(
        @PathVariable leagueId: Long
    ): ResponseEntity<ApiResponseV2<TeamsSyncResultDto>> {
        val result = adminApiSportsWebService.syncTeamsOfLeague(leagueId, null)
        
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            val httpStatus = result.code ?: 500
            ResponseEntity.status(httpStatus).body(result)
        }
    }

    /**
     * 특정 리그의 팀들을 지정된 시즌으로 동기화합니다.
     * 
     * POST /api/v1/admin/apisports/leagues/{leagueId}/teams/sync?season={season}
     * 
     * @param leagueId ApiSports 리그 ID
     * @param season 시즌 연도 (예: 2024)
     */
    @PostMapping("/leagues/{leagueId}/teams/sync", params = ["season"])
    fun syncTeamsOfLeagueWithSeason(
        @PathVariable leagueId: Long,
        @RequestParam season: Int
    ): ResponseEntity<ApiResponseV2<TeamsSyncResultDto>> {
        val result = adminApiSportsWebService.syncTeamsOfLeague(leagueId, season)
        
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            // 에러 코드에 따라 적절한 HTTP 상태 코드 반환
            val httpStatus = result.code ?: 500
            ResponseEntity.status(httpStatus).body(result)
        }
    }

    /**
     * 특정 팀의 선수들(스쿼드)을 동기화합니다.
     * 
     * POST /api/v1/admin/apisports/teams/{teamId}/players/sync
     * 
     * @param teamId ApiSports 팀 ID
     */
    @PostMapping("/teams/{teamId}/players/sync")
    fun syncPlayersOfTeam(
        @PathVariable teamId: Long
    ): ResponseEntity<ApiResponseV2<PlayersSyncResultDto>> {
        val result = adminApiSportsWebService.syncPlayersOfTeam(teamId)
        
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            // 에러 코드에 따라 적절한 HTTP 상태 코드 반환
            val httpStatus = result.code ?: 500
            ResponseEntity.status(httpStatus).body(result)
        }
    }

    /**
     * API 상태 확인용 헬스체크 엔드포인트
     * 
     * GET /api/v1/admin/apisports/health
     */
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<ApiResponseV2<Map<String, String>>> {
        val healthData = mapOf(
            "status" to "UP",
            "service" to "AdminApiSportsService",
            "timestamp" to System.currentTimeMillis().toString()
        )
        return ResponseEntity.ok(ApiResponseV2.success(healthData))
    }
} 