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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Admin Fixture Controller
 *
 * Admin 권한으로 Fixture의 available 상태를 관리하는 REST API입니다.
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
@RequestMapping("/api/v1/admin/fixtures")
class AdminFixtureController(
    private val availableFixtureFacade: AvailableFixtureFacade,
) {
    /**
     * Fixture를 Available로 설정
     *
     * Available로 설정하면 PreMatchJob이 등록되어 실시간 Match Data Sync가 시작됩니다.
     *
     * @param fixtureId FixtureCore ID
     * @return 성공 시 200 OK (fixture UID 반환)
     *         실패 시 404 Not Found (Fixture 없음)
     *               400 Bad Request (Job 등록 실패)
     */
    @Operation(
        summary = "경기 가용성 활성화 및 Job 등록",
        description =
            "경기를 available 상태로 설정하고 Match Data Sync Job을 등록합니다. " +
                "등록되는 Job: PreMatchJob (경기 1시간 전 시작, 라인업 수집), " +
                "LiveMatchJob (킥오프 시간부터 시작, 실시간 매치 데이터 polling). " +
                "Job 자동 전환: PreMatchJob 완료 → LiveMatchJob 시작 → LiveMatchJob 종료 감지 → PostMatchJob 자동 등록. " +
                "전제조건: 경기의 kickoff time이 설정되어 있어야 하며, 일정이 미정인 경기는 available로 설정할 수 없습니다.",
        operationId = "addAvailableFixture",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 경기를 available로 설정하고 Job을 등록함",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "success",
                                value = "apisports:1208021",
                                description = "등록된 경기의 UID (provider:id 형식)",
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
                description = "유효성 검증 실패 (kickoff time 미설정 등)",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "validation error",
                                value = "Validation error: Kickoff time must be set",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/{fixtureId}/available")
    fun addAvailableFixture(
        @Parameter(
            description = "FixtureCore ID (데이터베이스 내부 ID)",
            example = "12345",
            required = true,
        )
        @PathVariable fixtureId: Long,
    ): ResponseEntity<String> =
        when (val result = availableFixtureFacade.addAvailableFixture(fixtureId)) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
        }

    /**
     * Fixture를 Unavailable로 설정
     *
     * Unavailable로 설정하면 모든 Match Data Sync Job이 삭제됩니다.
     *
     * @param fixtureId FixtureCore ID
     * @return 성공 시 200 OK (fixture UID 반환)
     *         실패 시 404 Not Found (Fixture 없음)
     */
    @Operation(
        summary = "경기 가용성 비활성화 및 Job 삭제",
        description =
            "경기를 unavailable 상태로 설정하고 등록된 모든 Match Data Sync Job을 삭제합니다. " +
                "삭제되는 Job: PreMatchJob (경기 시작 전 라인업 수집), LiveMatchJob (실시간 매치 데이터 polling), " +
                "PostMatchJob (경기 종료 후 최종 통계 수집). " +
                "사용 시나리오: 경기가 취소된 경우, 라이브 데이터 수집을 중단하고 싶은 경우, Job 설정을 초기화하고 다시 등록하고 싶은 경우.",
        operationId = "removeAvailableFixture",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 경기를 unavailable로 설정하고 Job을 삭제함",
                content = [
                    Content(
                        examples = [
                            ExampleObject(
                                name = "success",
                                value = "apisports:1208021",
                                description = "삭제된 경기의 UID",
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
        ],
    )
    @DeleteMapping("/{fixtureId}/available")
    fun removeAvailableFixture(
        @Parameter(
            description = "FixtureCore ID (데이터베이스 내부 ID)",
            example = "12345",
            required = true,
        )
        @PathVariable fixtureId: Long,
    ): ResponseEntity<String> =
        when (val result = availableFixtureFacade.removeAvailableFixture(fixtureId)) {
            is DomainResult.Success -> ResponseEntity.ok(result.value)
            is DomainResult.Fail -> toErrorResponse(result.error)
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
