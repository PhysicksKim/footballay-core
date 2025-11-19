package com.footballay.core.web.admin.apisports.controller

import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.web.admin.apisports.dto.LeaguesSyncResultDto
import com.footballay.core.web.admin.apisports.dto.PlayersSyncResultDto
import com.footballay.core.web.admin.apisports.dto.LeagueSeasonRequest
import com.footballay.core.web.admin.apisports.dto.TeamsSyncResultDto
import com.footballay.core.web.admin.apisports.service.AdminApiSportsWebService
import com.footballay.core.web.admin.apisports.dto.AvailabilityToggleRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.footballay.core.web.admin.apisports.dto.AvailableLeagueDto
import com.footballay.core.web.admin.apisports.service.AdminLeagueQueryWebService
import com.footballay.core.web.admin.apisports.service.AdminFixtureQueryWebService
import com.footballay.core.web.admin.apisports.dto.FixtureSummaryDto
import com.footballay.core.web.admin.apisports.dto.PlayerAdminResponse
import com.footballay.core.web.admin.apisports.dto.TeamAdminResponse
import com.footballay.core.web.admin.apisports.service.AdminApiSportsQueryWebService
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@Tag(
    name = "Admin - ApiSports Sync",
    description =
        "ApiSports 데이터 동기화 API. " +
            "동기화 순서: 1) Leagues Sync (현재 시즌 리그), " +
            "2) Teams Sync (리그별 팀), " +
            "3) Players Sync (팀별 선수), " +
            "4) Fixtures Sync (리그별 경기 일정), " +
            "5) League Available (리그 활성화). " +
            "주의사항: 동기화는 순차적으로 진행되어야 하며, ApiSports API 호출 제한을 고려해야 합니다.",
)
@SecurityRequirement(name = "cookieAuth")
@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/apisports")
class AdminApiSportsController(
    private val adminApiSportsWebService: AdminApiSportsWebService,
    private val adminLeagueQueryWebService: AdminLeagueQueryWebService,
    private val adminFixtureQueryWebService: AdminFixtureQueryWebService,
    private val adminApiSportsQueryWebService: AdminApiSportsQueryWebService,
) {
    companion object {
        private const val OP_SYNC_LEAGUES =
            "ApiSports에서 현재 시즌의 모든 리그 정보를 가져와 동기화합니다. " +
                "동기화 프로세스: 1) ApiSports API에서 현재 시즌 리그 목록 조회, " +
                "2) LeagueApiSports 엔티티 생성/업데이트, " +
                "3) LeagueCore 엔티티 생성/업데이트. 첫 번째 단계: 데이터 동기화의 시작점입니다."

        private const val OP_SYNC_TEAMS =
            "특정 리그의 팀들을 현재 시즌 기준으로 동기화합니다. " +
                "동기화 프로세스: 1) ApiSports API에서 리그의 현재 시즌 팀 목록 조회, " +
                "2) TeamApiSports 엔티티 생성/업데이트, " +
                "3) TeamCore 엔티티 생성/업데이트. 전제조건: League Sync가 먼저 완료되어야 합니다."

        private const val OP_SYNC_PLAYERS =
            "특정 팀의 스쿼드(선수 명단)를 동기화합니다. " +
                "동기화 프로세스: 1) ApiSports API에서 팀의 선수 목록 조회, " +
                "2) PlayerApiSports 엔티티 생성/업데이트, " +
                "3) PlayerCore 엔티티 생성/업데이트. " +
                "전제조건: Team Sync가 먼저 완료되어야 합니다."

        private const val OP_SYNC_FIXTURES =
            "특정 리그의 경기 일정(Fixtures)을 동기화합니다. " +
                "동기화 프로세스: 1) ApiSports API에서 리그의 경기 일정 조회, " +
                "2) VenueApiSports 엔티티 생성/업데이트, " +
                "3) FixtureCore 엔티티 생성 (UID 자동 생성), " +
                "4) FixtureApiSports 엔티티 생성/업데이트. " +
                "전제조건: League Sync 완료, Team Sync 완료. " +
                "주의: 경기가 많은 리그는 API 호출 제한에 걸릴 수 있습니다."

        private const val OP_SET_LEAGUE_AVAILABLE =
            "리그를 available/unavailable 상태로 설정합니다. " +
                "Available 설정: 리그를 공개 API에 노출, 사용자가 해당 리그의 경기를 조회할 수 있게 됨. " +
                "사용 시나리오: 새 시즌이 시작되어 리그를 공개하는 경우, 특정 리그를 서비스에서 제외하는 경우. " +
                "주의: 리그의 경기들도 개별적으로 available 설정이 필요합니다."

        private const val OP_GET_TEAMS_BY_LEAGUE =
            "ApiSports League ID로 해당 리그의 팀 목록을 조회합니다. " +
                "Teams sync 후 결과 확인 또는 Players sync 전 팀 선택에 사용됩니다. " +
                "각 팀의 ApiSports 상세 정보(창단 연도, 로고 등)가 details 필드에 포함됩니다."

        private const val OP_GET_PLAYERS_BY_TEAM =
            "ApiSports Team ID로 해당 팀의 선수 목록을 조회합니다. " +
                "Players sync 후 결과 확인 및 선수 데이터 검증에 사용됩니다. " +
                "각 선수의 ApiSports 상세 정보(나이, 국적, 신장, 체중, 사진 등)가 details 필드에 포함됩니다."
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
                "status" to "up",
                "timestamp" to System.currentTimeMillis().toString(),
            )
        return ResponseEntity.ok(healthData)
    }

    @Operation(summary = "현재 시즌 리그 동기화", description = OP_SYNC_LEAGUES)
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = LeaguesSyncResultDto::class))]),
        ApiResponse(
            responseCode = "400",
            description = "요청 값이 유효하지 않음",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
    )
    @PostMapping("/leagues/sync")
    fun syncCurrentLeagues(): ResponseEntity<LeaguesSyncResultDto> =
        adminApiSportsWebService
            .syncCurrentLeagues()
            .toResponseEntity()

    @Operation(summary = "리그의 팀 동기화", description = OP_SYNC_TEAMS)
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = TeamsSyncResultDto::class))]),
        ApiResponse(responseCode = "404", description = "리그를 찾을 수 없음"),
        ApiResponse(
            responseCode = "400",
            description = "요청 값이 유효하지 않음",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
    )
    @PostMapping("/leagues/{leagueApiId}/teams/sync")
    fun syncTeamsOfLeague(
        @Parameter(description = "ApiSports 리그 ID", example = "39")
        @PathVariable
        @Positive
        leagueApiId: Long,
        @RequestBody(required = false)
        @Valid
        body: LeagueSeasonRequest?,
    ): ResponseEntity<TeamsSyncResultDto> =
        adminApiSportsWebService
            .syncTeamsOfLeague(leagueApiId, body?.season)
            .toResponseEntity()

    @Operation(summary = "팀의 선수 동기화", description = OP_SYNC_PLAYERS)
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = PlayersSyncResultDto::class))]),
        ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음"),
        ApiResponse(
            responseCode = "400",
            description = "요청 값이 유효하지 않음",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
    )
    @PostMapping("/teams/{teamApiId}/players/sync")
    fun syncPlayersOfTeam(
        @Parameter(description = "ApiSports 팀 ID", example = "50")
        @PathVariable
        @Positive
        teamApiId: Long,
    ): ResponseEntity<PlayersSyncResultDto> =
        adminApiSportsWebService
            .syncPlayersOfTeam(teamApiId)
            .toResponseEntity()

    @Operation(summary = "가용 리그 목록 조회")
    @ApiResponse(responseCode = "200")
    @GetMapping("/leagues/available")
    fun getAvailableLeagues(): ResponseEntity<List<AvailableLeagueDto>> = ResponseEntity.ok(adminLeagueQueryWebService.findAvailableLeagues())

    @Operation(summary = "리그별 픽스처 조회", description = "at(ISO-8601 UTC) 기준으로 exact/nearest 조회. at 미지정 시 서버 now 기준. mode 기본값 exact. timezone으로 날짜 계산 기준 지정 가능 (IANA format, default: UTC)")
    @ApiResponse(responseCode = "200")
    @GetMapping("/leagues/{leagueApiId}/fixtures")
    fun getLeagueFixtures(
        @Parameter(description = "ApiSports League ID", example = "39")
        @PathVariable
        @Positive
        leagueApiId: Long,
        @Parameter(description = "ISO-8601 UTC", example = "2025-03-05T10:00:00Z")
        @RequestParam(required = false)
        at: String?,
        @Parameter(description = "nearest | exact")
        @RequestParam(required = false, defaultValue = "exact")
        @Pattern(regexp = "exact|nearest")
        mode: String,
        @Parameter(description = "Timezone (IANA format, default: UTC)", example = "Asia/Seoul")
        @RequestParam(required = false, defaultValue = "UTC")
        timezone: String,
    ): ResponseEntity<List<FixtureSummaryDto>> {
        val atInstant =
            try {
                at?.let { Instant.parse(it) }
            } catch (_: Exception) {
                null
            }
        val zoneId =
            try {
                ZoneId.of(timezone)
            } catch (_: Exception) {
                ZoneOffset.UTC
            }
        return ResponseEntity.ok(adminFixtureQueryWebService.findFixturesByLeague(leagueApiId, atInstant, mode, zoneId))
    }

    @Operation(summary = "리그의 경기 일정 동기화", description = OP_SYNC_FIXTURES)
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "동기화된 경기 수 반환"),
        ApiResponse(responseCode = "404", description = "리그를 찾을 수 없음"),
        ApiResponse(
            responseCode = "400",
            description = "요청 값이 유효하지 않음",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
    )
    @PostMapping("/leagues/{leagueApiId}/fixtures/sync")
    fun syncFixturesOfLeague(
        @Parameter(description = "ApiSports League ID", example = "39")
        @PathVariable
        @Positive
        leagueApiId: Long,
    ): ResponseEntity<Int> =
        adminApiSportsWebService
            .syncFixturesOfLeague(leagueApiId)
            .toResponseEntity()

    @Operation(summary = "리그 available 설정", description = OP_SET_LEAGUE_AVAILABLE)
    @ApiResponses(
        ApiResponse(responseCode = "200"),
        ApiResponse(responseCode = "404", description = "리그를 찾을 수 없음"),
        ApiResponse(
            responseCode = "400",
            description = "요청 값이 유효하지 않음",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
    )
    @PutMapping("/leagues/{leagueApiId}/available")
    fun setLeagueAvailable(
        @Parameter(description = "ApiSports League ID", example = "39")
        @PathVariable
        @Positive
        leagueApiId: Long,
        @RequestBody
        @Valid
        request: AvailabilityToggleRequest,
    ): ResponseEntity<String> =
        adminApiSportsWebService
            .setLeagueAvailable(leagueApiId, request.available)
            .toResponseEntity()

    @Operation(summary = "리그별 팀 목록 조회", description = OP_GET_TEAMS_BY_LEAGUE)
    @ApiResponse(responseCode = "200")
    @GetMapping("/leagues/{leagueApiId}/teams")
    fun getTeamsByLeague(
        @Parameter(description = "ApiSports League ID", example = "39")
        @PathVariable
        leagueApiId: Long,
    ): ResponseEntity<List<TeamAdminResponse>> = ResponseEntity.ok(adminApiSportsQueryWebService.findTeamsByLeagueApiId(leagueApiId))

    @Operation(summary = "팀별 선수 목록 조회", description = OP_GET_PLAYERS_BY_TEAM)
    @ApiResponse(responseCode = "200")
    @GetMapping("/teams/{teamApiId}/players")
    fun getPlayersByTeam(
        @Parameter(description = "ApiSports Team ID", example = "50") @PathVariable teamApiId: Long,
    ): ResponseEntity<List<PlayerAdminResponse>> = ResponseEntity.ok(adminApiSportsQueryWebService.findPlayersByTeamApiId(teamApiId))
}
