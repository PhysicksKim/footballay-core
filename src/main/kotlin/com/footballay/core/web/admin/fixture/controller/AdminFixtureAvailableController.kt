package com.footballay.core.web.admin.fixture.controller

import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.facade.AvailableFixtureFacade
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import com.footballay.core.web.admin.common.dto.AvailabilityToggleRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwagRequestBody

/**
 * Fixture의 available 상태를 관리합니다.
 *
 * fixture available 은 false <-> true 토글 방식으로 동작하며, Live Match data polling 의 트리거가 됩니다.
 *
 * **주요 기능:**
 * - Fixture를 available로 설정하여 실시간 Match Data Sync 활성화
 * - Fixture를 unavailable로 설정하여 실시간 Sync 비활성화
 *
 * **엔드포인트:**
 * - POST   /api/v1/admin/fixtures/{fixtureId}/available - Available 설정
 * - DELETE /api/v1/admin/fixtures/{fixtureId}/available - Available 해제
 */
@Tag(
    name = "Admin - Fixture Management",
    description =
        "Fixture Available 및 Match Data Sync Job 관리 API. \n" +
            "Job 등록 프로세스: 1) POST /available - PreMatchJob + LiveMatchJob 등록 (경기 1시간 전부터 시작), " +
            "2) PreMatchJob - 라인업 데이터 수집, " +
            "3) LiveMatchJob - 실시간 매치 데이터 polling, " +
            "4) PostMatchJob - 경기 종료 후 자동 등록, 최종 통계 수집. 전제조건: 경기의 kickoff time이 반드시 설정되어 있어야 합니다.",
)
@SecurityRequirement(name = "cookieAuth")
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/fixtures")
class AdminFixtureAvailableController(
    private val availableFixtureFacade: AvailableFixtureFacade,
) {
    /**
     * Fixture available 설정(활성/비활성) - PUT + JSON 바디
     */
    @Operation(
        summary = "경기 available 설정",
        description =
            "경기를 available/unavailable 상태로 설정합니다. " +
                "available=true인 경우: PreMatchJob 등록 후 LiveMatchJob으로 이어집니다. " +
                "available=false인 경우: 등록된 Match Data Sync Job이 삭제됩니다.",
        operationId = "setFixtureAvailable",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 available 상태를 변경함",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "success",
                                value = "apisports:1208021",
                                description = "대상 경기의 UID",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "경기를 찾을 수 없음",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "not found",
                                value = "Fixture not found: 12345",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효성 검증 실패",
            ),
        ],
    )
    @PutMapping("/{fixtureId}/available")
    fun setFixtureAvailable(
        @Parameter(
            description = "ApiSports Fixture ID",
            example = "1208021",
            required = true,
        )
        @PathVariable fixtureId: Long,
        @SwagRequestBody(
            description = "available 토글 요청 바디",
            required = true,
            content = [
                Content(
                    examples = [
                        ExampleObject(
                            name = "enable",
                            value = "{\\n  \"available\": true\\n}",
                        ),
                        ExampleObject(
                            name = "disable",
                            value = "{\\n  \"available\": false\\n}",
                        ),
                    ],
                ),
            ],
        )
        @RequestBody request: AvailabilityToggleRequest,
    ): ResponseEntity<String> =
        if (request.available) {
            when (val result = availableFixtureFacade.addAvailableFixture(fixtureId)) {
                is DomainResult.Success -> ResponseEntity.ok(result.value)
                is DomainResult.Fail -> toErrorResponse(result.error)
            }
        } else {
            when (val result = availableFixtureFacade.removeAvailableFixture(fixtureId)) {
                is DomainResult.Success -> ResponseEntity.ok(result.value)
                is DomainResult.Fail -> toErrorResponse(result.error)
            }
        }

    /**
     * DomainFail을 HTTP 응답으로 변환하는 헬퍼 메서드
     */
    private fun toErrorResponse(error: com.footballay.core.common.result.DomainFail): ResponseEntity<String> =
        when (error) {
            is com.footballay.core.common.result.DomainFail.NotFound ->
                ResponseEntity.status(404).body("Fixture not found: ${error.id}")

            is com.footballay.core.common.result.DomainFail.Validation ->
                ResponseEntity.status(400).body("Validation error: ${error.errors.joinToString { it.message }}")
        }
}
