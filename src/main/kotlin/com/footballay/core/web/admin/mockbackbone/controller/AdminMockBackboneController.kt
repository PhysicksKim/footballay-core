package com.footballay.core.web.admin.mockbackbone.controller

import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.web.admin.apisports.controller.ValidationErrorResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockFixtureCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockFixtureResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockLeagueCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockLeagueResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockSimpleFixtureScenarioCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockSimpleFixtureScenarioResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockTeamCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockTeamResponse
import com.footballay.core.web.admin.mockbackbone.service.AdminMockBackboneWebService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Admin - MockBackbone",
    description =
        "MockBackbone 리그/팀/경기 생성 및 삭제 API. " +
            "MockBackbone 은 Core entity 를 생성하는 개발용 backbone 이며, available job 생성은 기존 Core UID available API 경로에서 처리합니다.",
)
@SecurityRequirement(name = "cookieAuth")
@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(
    name = ["footballay.mock-backbone.admin-api.enabled"],
    havingValue = "true",
)
@RequestMapping("/api/v1/admin/mock")
class AdminMockBackboneController(
    private val adminMockBackboneWebService: AdminMockBackboneWebService,
) {
    @Operation(
        summary = "mock 리그 생성",
        description = "MockBackbone 리그를 생성하고 생성된 LeagueCore 기반 응답을 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "생성된 mock 리그 반환",
            content = [Content(schema = Schema(implementation = MockLeagueResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "요청 값이 유효하지 않음",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
    )
    @PostMapping("/leagues")
    fun createLeague(
        @RequestBody @Valid request: MockLeagueCreateRequest,
    ): ResponseEntity<MockLeagueResponse> =
        adminMockBackboneWebService
            .createLeague(request)
            .toResponseEntity()

    @Operation(
        summary = "mock 리그 삭제",
        description = "LeagueCore UID로 MockBackbone 리그를 삭제하고 삭제된 리그 정보를 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "삭제된 mock 리그 반환",
            content = [Content(schema = Schema(implementation = MockLeagueResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "mock 리그를 찾을 수 없음"),
    )
    @DeleteMapping("/leagues/{leagueCoreUid}")
    fun deleteLeague(
        @Parameter(description = "LeagueCore UID", example = "league_core_abcd1234")
        @PathVariable leagueCoreUid: String,
    ): ResponseEntity<MockLeagueResponse> =
        adminMockBackboneWebService
            .deleteLeague(leagueCoreUid)
            .toResponseEntity()

    @Operation(
        summary = "mock 팀 생성",
        description = "MockBackbone 팀을 생성하고 생성된 TeamCore 기반 응답을 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "생성된 mock 팀 반환",
            content = [Content(schema = Schema(implementation = MockTeamResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "요청 값이 유효하지 않음",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
    )
    @PostMapping("/teams")
    fun createTeam(
        @RequestBody @Valid request: MockTeamCreateRequest,
    ): ResponseEntity<MockTeamResponse> =
        adminMockBackboneWebService
            .createTeam(request)
            .toResponseEntity()

    @Operation(
        summary = "mock 팀 삭제",
        description = "TeamCore UID로 MockBackbone 팀을 삭제하고 삭제된 팀 정보를 반환합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "삭제된 mock 팀 반환",
            content = [Content(schema = Schema(implementation = MockTeamResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "mock 팀을 찾을 수 없음"),
    )
    @DeleteMapping("/teams/{teamCoreUid}")
    fun deleteTeam(
        @Parameter(description = "TeamCore UID", example = "team_core_abcd1234")
        @PathVariable teamCoreUid: String,
    ): ResponseEntity<MockTeamResponse> =
        adminMockBackboneWebService
            .deleteTeam(teamCoreUid)
            .toResponseEntity()

    @Operation(
        summary = "mock 경기 생성",
        description =
            "MockBackbone 경기를 생성하고 생성된 FixtureCore 기반 응답을 반환합니다. " +
                "생성 시 fixture available 은 항상 false 이며, available job 검증은 Core UID available API로 수행합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "생성된 mock 경기 반환",
            content = [Content(schema = Schema(implementation = MockFixtureResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "요청 값이 유효하지 않음",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "리그 또는 팀을 찾을 수 없음"),
    )
    @PostMapping("/fixtures")
    fun createFixture(
        @RequestBody @Valid request: MockFixtureCreateRequest,
    ): ResponseEntity<MockFixtureResponse> =
        adminMockBackboneWebService
            .createFixture(request)
            .toResponseEntity()

    @Operation(
        summary = "mock 경기 삭제",
        description =
            "FixtureCore UID로 MockBackbone 경기를 삭제하고 삭제된 경기 정보를 반환합니다. " +
                "경기가 available 상태라면 기존 available facade 경로로 job 정리를 먼저 수행합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "삭제된 mock 경기 반환",
            content = [Content(schema = Schema(implementation = MockFixtureResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "mock 경기를 찾을 수 없음"),
    )
    @DeleteMapping("/fixtures/{fixtureCoreUid}")
    fun deleteFixture(
        @Parameter(description = "FixtureCore UID", example = "fixture_core_abcd1234")
        @PathVariable fixtureCoreUid: String,
    ): ResponseEntity<MockFixtureResponse> =
        adminMockBackboneWebService
            .deleteFixture(fixtureCoreUid)
            .toResponseEntity()

    @Operation(
        summary = "simple fixture scenario 생성",
        description = "mock 리그, 홈팀, 원정팀, 경기를 한 번에 생성하는 편의 API입니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "생성된 simple fixture scenario 반환",
            content = [Content(schema = Schema(implementation = MockSimpleFixtureScenarioResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "요청 값이 유효하지 않음",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
    )
    @PostMapping("/scenarios/simple-fixture")
    fun createSimpleFixtureScenario(
        @RequestBody @Valid request: MockSimpleFixtureScenarioCreateRequest,
    ): ResponseEntity<MockSimpleFixtureScenarioResponse> =
        adminMockBackboneWebService
            .createSimpleFixtureScenario(request)
            .toResponseEntity()

    @Operation(
        summary = "simple fixture scenario 삭제",
        description = "FixtureCore UID를 기준으로 simple fixture scenario 의 mock 리그, 팀, 경기를 삭제합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "삭제된 simple fixture scenario 반환",
            content = [Content(schema = Schema(implementation = MockSimpleFixtureScenarioResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "simple fixture scenario 를 찾을 수 없음"),
    )
    @DeleteMapping("/scenarios/simple-fixture/by-fixture/{fixtureCoreUid}")
    fun deleteSimpleFixtureScenarioByFixtureUid(
        @Parameter(description = "FixtureCore UID", example = "fixture_core_abcd1234")
        @PathVariable fixtureCoreUid: String,
    ): ResponseEntity<MockSimpleFixtureScenarioResponse> =
        adminMockBackboneWebService
            .deleteSimpleFixtureScenarioByFixtureUid(fixtureCoreUid)
            .toResponseEntity()
}
