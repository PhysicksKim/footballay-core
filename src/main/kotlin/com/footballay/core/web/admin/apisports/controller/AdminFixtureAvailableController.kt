package com.footballay.core.web.admin.apisports.controller

import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.web.admin.apisports.dto.ToggleAvailableResponse
import com.footballay.core.web.admin.apisports.service.AdminFixtureAvailableWebService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import com.footballay.core.web.admin.common.dto.AvailabilityToggleRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(
    name = "Admin - ApiSports Fixture Management",
    description =
        "Fixture Available 및 Match Data Sync Job 관리 API. " +
            "Job 등록 프로세스: 1) PUT /available - PreMatchJob + LiveMatchJob 등록 (경기 1시간 전부터 시작), " +
            "2) PreMatchJob - 라인업 데이터 수집, " +
            "3) LiveMatchJob - 실시간 매치 데이터 polling, " +
            "4) PostMatchJob - 경기 종료 후 자동 등록, 최종 통계 수집. 전제조건: 경기의 kickoff time이 반드시 설정되어 있어야 합니다.",
)
@SecurityRequirement(name = "cookieAuth")
@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/apisports/fixtures")
class AdminFixtureAvailableController(
    private val adminFixtureAvailableWebService: AdminFixtureAvailableWebService,
) {
    companion object {
        private const val OP_SET_FIXTURE_AVAILABLE =
            "경기를 available/unavailable 상태로 설정합니다. " +
                "available=true인 경우: PreMatchJob 등록 후 LiveMatchJob으로 이어집니다. " +
                "available=false인 경우: 등록된 Match Data Sync Job이 삭제됩니다."
    }

    @Operation(summary = "경기 available 설정", description = OP_SET_FIXTURE_AVAILABLE)
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "대상 경기의 UID 및 available 상태 반환",
            content = [Content(schema = Schema(implementation = ToggleAvailableResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "경기를 찾을 수 없음"),
        ApiResponse(
            responseCode = "400",
            description = "유효성 검증 실패 (Bean Validation)",
            content = [Content(schema = Schema(implementation = ValidationErrorResponse::class))],
        ),
    )
    @PutMapping("/{fixtureApiId}/available")
    fun setFixtureAvailable(
        @Parameter(description = "ApiSports Fixture ID", example = "1208021")
        @PathVariable
        @Positive
        fixtureApiId: Long,
        @RequestBody
        @Valid
        request: AvailabilityToggleRequest,
    ): ResponseEntity<ToggleAvailableResponse> =
        adminFixtureAvailableWebService
            .setFixtureAvailable(fixtureApiId, request.available)
            .toResponseEntity()
}
