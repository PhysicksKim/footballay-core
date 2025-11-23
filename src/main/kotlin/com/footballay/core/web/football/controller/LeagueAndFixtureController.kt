package com.footballay.core.web.football.controller

import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.web.football.dto.AvailableLeagueResponse
import com.footballay.core.web.football.dto.FixtureByLeagueResponse
import com.footballay.core.web.football.service.LeagueAndFixtureWebService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@Tag(
    name = "Football - Leagues",
    description = "Desktop App용 UID 기반 리그 및 경기 일정 조회 API",
)
@Validated
@RestController
@RequestMapping("/api/v1/football/leagues")
class LeagueAndFixtureController(
    private val leagueAndFixtureWebService: LeagueAndFixtureWebService,
) {
    @Operation(summary = "가용 리그 목록 조회", description = "Available한 모든 리그를 조회합니다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/available")
    fun availableLeagues(): ResponseEntity<List<AvailableLeagueResponse>> = leagueAndFixtureWebService.getAvailableLeagues().toResponseEntity()

    /**
     * 리그의 경기 일정을 모드에 따라 조회합니다.
     *
     * mode:
     * - previous: 기준 날짜 이전 가장 가까운 날짜의 경기들
     * - exact: 정확히 해당 날짜의 경기들 (default)
     * - nearest: 기준 날짜 이후 가장 가까운 날짜의 경기들
     */
    @Operation(
        summary = "리그별 경기 일정 조회",
        description =
            "리그 UID로 경기 일정을 조회합니다. " +
                "mode: previous(이전 가장 가까운 날), exact(정확히 해당 날), nearest(이후 가장 가까운 날). " +
                "date 미지정 시 현재 날짜 기준.",
    )
    @ApiResponse(responseCode = "200")
    @GetMapping("/{leagueUid}/fixtures")
    fun fixturesByLeague(
        @Parameter(description = "리그 UID", example = "a1b2c3d4e5f6g7h8")
        @PathVariable
        leagueUid: String,
        @Parameter(description = "날짜 (YYYY-MM-DD), 미지정 시 현재 날짜", example = "2025-12-25")
        @RequestParam(required = false)
        date: String?,
        @Parameter(description = "previous | exact | nearest")
        @RequestParam(required = false, defaultValue = "exact")
        @Pattern(regexp = "previous|exact|nearest")
        mode: String,
        @Parameter(description = "Timezone (IANA format, default: UTC)", example = "Asia/Seoul")
        @RequestParam(required = false, defaultValue = "UTC")
        timezone: String,
    ): ResponseEntity<List<FixtureByLeagueResponse>> {
        val zoneId =
            try {
                ZoneId.of(timezone)
            } catch (_: Exception) {
                ZoneOffset.UTC
            }
        val localDate =
            try {
                date?.let { LocalDate.parse(it) }
            } catch (_: Exception) {
                null
            }
        val atInstant = localDate?.atStartOfDay(zoneId)?.toInstant()

        return leagueAndFixtureWebService
            .getFixturesByLeague(leagueUid, atInstant, mode, zoneId)
            .toResponseEntity()
    }
}
