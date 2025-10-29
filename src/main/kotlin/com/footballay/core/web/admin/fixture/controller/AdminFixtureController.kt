package com.footballay.core.web.admin.fixture.controller

import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.facade.AvailableFixtureFacade
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
    @PostMapping("/{fixtureId}/available")
    fun addAvailableFixture(
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
    @DeleteMapping("/{fixtureId}/available")
    fun removeAvailableFixture(
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

