package com.footballay.core.web.admin.apisports.controller

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.web.admin.apisports.dto.LeaguesSyncResultDto
import com.footballay.core.web.admin.apisports.dto.PlayersSyncResultDto
import com.footballay.core.web.admin.apisports.dto.TeamsSyncResultDto
import com.footballay.core.web.admin.apisports.service.AdminApiSportsWebService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/apisports")
class AdminApiSportsController(
    private val adminApiSportsWebService: AdminApiSportsWebService,
) {
    /**
     * Controller 헬스 테스트용 엔드포인트
     *
     * GET /api/v1/admin/apisports/test
     */
    @GetMapping("/test")
    fun testAdminController(): ResponseEntity<String> = ResponseEntity.ok("Admin API Sports Controller is working!")

    /**
     * 현재 시즌의 모든 리그를 동기화합니다.
     *
     * POST /api/v1/admin/apisports/leagues/sync
     */
    @PostMapping("/leagues/sync")
    fun syncCurrentLeagues(): ResponseEntity<LeaguesSyncResultDto> =
        when (val result = adminApiSportsWebService.syncCurrentLeagues()) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
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
        @PathVariable leagueId: Long,
    ): ResponseEntity<TeamsSyncResultDto> =
        when (val result = adminApiSportsWebService.syncTeamsOfLeague(leagueId, null)) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
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
        @RequestParam season: Int,
    ): ResponseEntity<TeamsSyncResultDto> =
        when (val result = adminApiSportsWebService.syncTeamsOfLeague(leagueId, season)) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
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
        @PathVariable teamId: Long,
    ): ResponseEntity<PlayersSyncResultDto> =
        when (val result = adminApiSportsWebService.syncPlayersOfTeam(teamId)) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
        }

    /**
     * API 상태 확인용 헬스체크 엔드포인트
     *
     * GET /api/v1/admin/apisports/health
     */
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        val healthData =
            mapOf(
                "status" to "UP",
                "service" to "AdminApiSportsService",
                "timestamp" to System.currentTimeMillis().toString(),
            )
        return ResponseEntity.ok(healthData)
    }

    /**
     * 특정 리그의 Fixture 들을 저장합니다.
     *
     * POST /api/v1/admin/apisports/leagues/{leagueId}/fixtures/sync
     * @param leagueId ApiSports 리그 ID
     */
    @PostMapping("/leagues/{leagueId}/fixtures/sync")
    fun syncFixturesOfLeague(
        @PathVariable leagueId: Long,
    ): ResponseEntity<Int> =
        when (val result = adminApiSportsWebService.syncFixturesOfLeague(leagueId)) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
        }

    /**
     * 리그의 Available 상태를 설정합니다.
     *
     * POST /api/v1/admin/apisports/leagues/{leagueId}/available?available=true|false
     *
     * @param leagueId LeagueCore ID
     * @param available Available 상태 (true: 활성화, false: 비활성화)
     */
    @PostMapping("/leagues/{leagueId}/available")
    fun setLeagueAvailable(
        @PathVariable leagueId: Long,
        @RequestParam available: Boolean,
    ): ResponseEntity<String> =
        when (val result = adminApiSportsWebService.setLeagueAvailable(leagueId, available)) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
        }

    private fun <T> toErrorResponse(error: DomainFail): ResponseEntity<T> {
        val status =
            when (error) {
                is DomainFail.Validation -> HttpStatus.BAD_REQUEST
                is DomainFail.NotFound -> HttpStatus.NOT_FOUND
            }
        return ResponseEntity.status(status).build()
    }
}
