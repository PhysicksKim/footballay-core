package com.footballay.core.web.admin.match.controller

import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.web.admin.match.service.MatchSyncWebService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@Tag(
    name = "Admin - Fixture Match Management",
    description = "UID 기반 경기 매치 데이터 동기화 관리 API",
)
@SecurityRequirement(name = "cookieAuth")
@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/fixtures")
class AdminFixtureMatchController(
    private val matchSyncWebService: MatchSyncWebService,
) {
    companion object {
        private const val OP_MATCH_SYNC_ONCE =
            "Match Data 를 한번만 sync 요청 합니다." +
                "주로 의도적으로 경기 데이터 저장 후 제대로 동작하는지 검증하기 위해 사용합니다." +
                "Available 상태와 무관하게 동작합니다."
    }

    @Operation(summary = "Match Sync Once", description = OP_MATCH_SYNC_ONCE)
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "경기 저장 성공"),
        ApiResponse(responseCode = "400", description = "유효성 검증 실패 (Bean Validation)"),
        ApiResponse(responseCode = "404", description = "경기를 찾을 수 없거나 저장할 수 없는 경기"),
    )
    @PostMapping("/{fixtureUid}/match/sync")
    fun setFixtureAvailable(
        @Parameter(description = "Fixture UID", example = "dp4tqrssmv7fid8v")
        @PathVariable
        @NotBlank
        fixtureUid: String,
    ): ResponseEntity<Unit> =
        matchSyncWebService
            .syncMatchOnce(fixtureUid)
            .toResponseEntity()
}
