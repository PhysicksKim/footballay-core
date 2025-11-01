package com.footballay.core.web.admin.apisports.controller

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.web.admin.apisports.dto.LeaguesSyncResultDto
import com.footballay.core.web.admin.apisports.dto.PlayersSyncResultDto
import com.footballay.core.web.admin.apisports.dto.SeasonSyncRequestDto
import com.footballay.core.web.admin.apisports.dto.TeamsSyncResultDto
import com.footballay.core.web.admin.apisports.service.AdminApiSportsWebService
import com.footballay.core.web.admin.common.dto.AvailabilityToggleRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.footballay.core.web.admin.league.dto.AvailableLeagueDto
import com.footballay.core.web.admin.league.service.AdminLeagueQueryWebService
import com.footballay.core.web.admin.fixture.service.AdminFixtureQueryWebService
import com.footballay.core.web.admin.fixture.dto.FixtureSummaryDto
import org.springframework.security.access.prepost.PreAuthorize
import java.time.Instant
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwagRequestBody

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
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/apisports")
class AdminApiSportsController(
    private val adminApiSportsWebService: AdminApiSportsWebService,
    private val adminLeagueQueryWebService: AdminLeagueQueryWebService,
    private val adminFixtureQueryWebService: AdminFixtureQueryWebService,
) {
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

    /**
     * 현재 시즌의 모든 리그를 동기화합니다.
     *
     * POST /api/v1/admin/apisports/leagues/sync
     */
    @Operation(
        summary = "현재 시즌 리그 동기화",
        description =
            "ApiSports에서 현재 시즌의 모든 리그 정보를 가져와 동기화합니다. " +
                "동기화 프로세스: 1) ApiSports API에서 현재 시즌 리그 목록 조회, " +
                "2) LeagueApiSports 엔티티 생성/업데이트, " +
                "3) LeagueCore 엔티티 생성/업데이트. 첫 번째 단계: 데이터 동기화의 시작점입니다.",
        operationId = "syncCurrentLeagues",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "리그 동기화 성공",
                content = [Content(schema = Schema(implementation = LeaguesSyncResultDto::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "동기화 실패 (API 오류 등)",
            ),
        ],
    )
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
    @Operation(
        summary = "리그의 팀 동기화 (현재 시즌)",
        description =
            "특정 리그의 팀들을 현재 시즌 기준으로 동기화합니다. " +
                "동기화 프로세스: 1) ApiSports API에서 리그의 현재 시즌 팀 목록 조회, " +
                "2) TeamApiSports 엔티티 생성/업데이트, " +
                "3) TeamCore 엔티티 생성/업데이트. 전제조건: League Sync가 먼저 완료되어야 합니다.",
        operationId = "syncTeamsOfLeagueCurrentSeason",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "팀 동기화 성공",
                content = [Content(schema = Schema(implementation = TeamsSyncResultDto::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "리그를 찾을 수 없음",
            ),
            ApiResponse(
                responseCode = "400",
                description = "동기화 실패",
            ),
        ],
    )
    @PostMapping("/leagues/{leagueId}/teams/sync")
    fun syncTeamsOfLeague(
        @Parameter(
            description = "ApiSports 리그 ID",
            example = "39",
            required = true,
        )
        @PathVariable leagueId: Long,
        @SwagRequestBody(
            description = "동기화 대상 시즌 정보 (없으면 현재 시즌으로 동기화)",
            required = false,
            content = [
                Content(
                    schema = Schema(implementation = SeasonSyncRequestDto::class),
                    examples = [
                        ExampleObject(
                            name = "현재 시즌 동기화",
                            value = """{}""",
                        ),
                        ExampleObject(
                            name = "특정 시즌 동기화",
                            value = """{ "season": 2024 }""",
                        ),
                    ],
                ),
            ],
        )
        @RequestBody(required = false) body: SeasonSyncRequestDto?,
    ): ResponseEntity<TeamsSyncResultDto> {
        val season = body?.season
        val result = adminApiSportsWebService.syncTeamsOfLeague(leagueId, season)
        return when (result) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
        }
    }

    /**
     * 특정 팀의 선수들(스쿼드)을 동기화합니다.
     *
     * POST /api/v1/admin/apisports/teams/{teamId}/players/sync
     *
     * @param teamId ApiSports 팀 ID
     */
    @Operation(
        summary = "팀의 선수 동기화",
        description =
            "특정 팀의 스쿼드(선수 명단)를 동기화합니다. " +
                "동기화 프로세스: 1) ApiSports API에서 팀의 선수 목록 조회, " +
                "2) PlayerApiSports 엔티티 생성/업데이트, " +
                "3) PlayerCore 엔티티 생성/업데이트. " +
                "전제조건: Team Sync가 먼저 완료되어야 합니다.",
        operationId = "syncPlayersOfTeam",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "선수 동기화 성공",
                content = [Content(schema = Schema(implementation = PlayersSyncResultDto::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "팀을 찾을 수 없음",
            ),
            ApiResponse(
                responseCode = "400",
                description = "동기화 실패",
            ),
        ],
    )
    @PostMapping("/teams/{teamId}/players/sync")
    fun syncPlayersOfTeam(
        @Parameter(
            description = "ApiSports 팀 ID",
            example = "50",
            required = true,
        )
        @PathVariable teamId: Long,
    ): ResponseEntity<PlayersSyncResultDto> =
        when (val result = adminApiSportsWebService.syncPlayersOfTeam(teamId)) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
        }

    /**
     * 가용 리그 목록 조회
     *
     * GET /api/v1/admin/apisports/leagues/available
     */
    @Operation(
        summary = "가용 리그 목록 조회",
        description = "현재 available=true로 설정된 리그 목록을 반환합니다.",
        operationId = "getAvailableLeagues",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = AvailableLeagueDto::class))],
            ),
        ],
    )
    @GetMapping("/leagues/available")
    fun getAvailableLeagues(): ResponseEntity<List<AvailableLeagueDto>> = ResponseEntity.ok(adminLeagueQueryWebService.findAvailableLeagues())

    /**
     * 리그별 픽스처 조회 (시간: ISO-8601 UTC)
     *
     * GET /api/v1/admin/apisports/leagues/{leagueId}/fixtures?at=2025-03-04T00:00:00Z&mode=nearest|exact
     */
    @Operation(
        summary = "리그별 픽스처 조회",
        description =
            "리그의 픽스처를 조회합니다. at(ISO-8601 UTC) 기준으로 exact/nearest를 처리하며, at 미지정 시 서버 now를 기준으로 합니다.",
        operationId = "getLeagueFixtures",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = FixtureSummaryDto::class))],
            ),
        ],
    )
    @GetMapping("/leagues/{leagueId}/fixtures")
    fun getLeagueFixtures(
        @Parameter(description = "LeagueCore ID", required = true, example = "1")
        @PathVariable leagueId: Long,
        @Parameter(description = "ISO-8601 UTC. 생략 시 서버 now")
        @RequestParam(required = false) at: String?,
        @Parameter(description = "nearest | exact. 기본값 exact")
        @RequestParam(required = false, defaultValue = "exact") mode: String,
    ): ResponseEntity<List<FixtureSummaryDto>> {
        val atInstant =
            try {
                at?.let { Instant.parse(it) }
            } catch (e: Exception) {
                null
            }
        return ResponseEntity.ok(adminFixtureQueryWebService.findFixturesByLeague(leagueId, atInstant, mode))
    }

    /**
     * 특정 리그의 Fixture 들을 저장합니다.
     *
     * POST /api/v1/admin/apisports/leagues/{leagueId}/fixtures/sync
     * @param leagueId ApiSports 리그 ID
     */
    @Operation(
        summary = "리그의 경기 일정 동기화",
        description =
            "특정 리그의 경기 일정(Fixtures)을 동기화합니다. " +
                "동기화 프로세스: 1) ApiSports API에서 리그의 경기 일정 조회, " +
                "2) VenueApiSports 엔티티 생성/업데이트, " +
                "3) FixtureCore 엔티티 생성 (UID 자동 생성), " +
                "4) FixtureApiSports 엔티티 생성/업데이트. " +
                "전제조건: League Sync 완료, Team Sync 완료. " +
                "주의: 경기가 많은 리그는 API 호출 제한에 걸릴 수 있습니다.",
        operationId = "syncFixturesOfLeague",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "경기 일정 동기화 성공, 동기화된 경기 수 반환",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "success",
                                value = "42",
                                description = "동기화된 경기 수",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "리그를 찾을 수 없음",
            ),
            ApiResponse(
                responseCode = "400",
                description = "동기화 실패",
            ),
        ],
    )
    @PostMapping("/leagues/{leagueId}/fixtures/sync")
    fun syncFixturesOfLeague(
        @Parameter(
            description = "ApiSports 리그 ID",
            example = "39",
            required = true,
        )
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
    @Operation(
        summary = "리그 available 설정",
        description =
            "리그를 available/unavailable 상태로 설정합니다. " +
                "Available 설정: 리그를 공개 API에 노출, 사용자가 해당 리그의 경기를 조회할 수 있게 됨. " +
                "사용 시나리오: 새 시즌이 시작되어 리그를 공개하는 경우, 특정 리그를 서비스에서 제외하는 경우. " +
                "주의: 리그의 경기들도 개별적으로 available 설정이 필요합니다.",
        operationId = "setLeagueAvailable",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "리그 available 설정 성공",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "success",
                                value = "League availability updated successfully",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "리그를 찾을 수 없음",
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효성 검증 실패",
            ),
        ],
    )
    @PutMapping("/leagues/{leagueId}/available")
    fun setLeagueAvailable(
        @Parameter(
            description = "ApiSports League ID",
            example = "39",
            required = true,
        )
        @PathVariable leagueId: Long,
        @SwagRequestBody(
            description = "available 토글 요청 바디",
            required = true,
            content = [
                Content(
                    examples = [
                        ExampleObject(
                            name = "enable",
                            value = "{\n  \"available\": true\n}",
                        ),
                        ExampleObject(
                            name = "disable",
                            value = "{\n  \"available\": false\n}",
                        ),
                    ],
                ),
            ],
        )
        @RequestBody request: AvailabilityToggleRequest,
    ): ResponseEntity<String> =
        when (val result = adminApiSportsWebService.setLeagueAvailable(leagueId, request.available)) {
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
