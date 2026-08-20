package com.footballay.core.web.football.controller

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.toHttpStatus
import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.logger
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.football.dto.AvailableLeagueResponse
import com.footballay.core.web.football.dto.FixtureByLeagueResponse
import com.footballay.core.web.football.dto.FixtureDatesByLeagueResponse
import com.footballay.core.web.football.service.LeagueAndFixtureWebService
import com.footballay.core.web.football.service.MockDataReadOptionResolver
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestHeader
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
    private val localeResolver: AcceptLanguageLocaleResolver,
) {
    val log = logger()

    @Operation(summary = "가용 리그 목록 조회", description = "Available한 모든 리그를 조회합니다.")
    @ApiResponse(responseCode = "200")
    @GetMapping("/available")
    fun availableLeagues(
        @Parameter(description = "mock data 포함 옵션. include이면 mock data를 포함합니다.", example = "include")
        @RequestHeader(name = MockDataReadOptionResolver.HEADER_NAME, required = false)
        devData: String?,
        @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false)
        acceptLanguage: String?,
    ): ResponseEntity<List<AvailableLeagueResponse>> {
        val locale = localeResolver.resolve(acceptLanguage)
        return toLocalizedResponse(
            leagueAndFixtureWebService.getAvailableLeagues(
                MockDataReadOptionResolver.resolve(devData),
                locale,
            ),
            locale,
        )
    }

    @Operation(summary = "리그별 경기 날짜 조회", description = "캘린더 표시에 사용할 경기 보유 날짜를 조회합니다.")
    @ApiResponse(
        responseCode = "200",
        description = "경기가 있는 날짜 목록입니다.",
        content = [
            Content(
                schema = Schema(implementation = FixtureDatesByLeagueResponse::class),
                examples = [ExampleObject(value = """{"dates":["2026-08-01","2026-08-31"]}""")],
            ),
        ],
    )
    @GetMapping("/{leagueUid}/fixtures/dates")
    fun fixtureDatesByLeague(
        @Parameter(description = "리그 UID", example = "a1b2c3d4e5f6g7h8")
        @PathVariable
        leagueUid: String,
        @Parameter(description = "시작 날짜 (YYYY-MM-DD, inclusive)", example = "2026-08-01")
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate,
        @Parameter(description = "종료 날짜 (YYYY-MM-DD, inclusive)", example = "2026-08-31")
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate,
        @Parameter(description = "Timezone (IANA format, default: UTC)", example = "Asia/Seoul")
        @RequestParam(required = false, defaultValue = "UTC")
        timezone: String,
        @Parameter(description = "mock data 포함 옵션. include이면 mock data를 포함합니다.", example = "include")
        @RequestHeader(name = MockDataReadOptionResolver.HEADER_NAME, required = false)
        devData: String?,
    ): ResponseEntity<FixtureDatesByLeagueResponse> {
        val normalizedLeagueUid = leagueUid.trim()
        if (normalizedLeagueUid.isEmpty()) return ResponseEntity.badRequest().build()
        val zoneId =
            try {
                ZoneId.of(timezone.trim())
            } catch (_: Exception) {
                return ResponseEntity.badRequest().build()
            }
        if (startDate.isAfter(endDate)) return ResponseEntity.badRequest().build()

        return leagueAndFixtureWebService
            .getFixtureDatesByLeague(
                normalizedLeagueUid,
                startDate.atStartOfDay(zoneId).toInstant(),
                endDate.plusDays(1).atStartOfDay(zoneId).toInstant(),
                zoneId,
                MockDataReadOptionResolver.resolve(devData),
            ).toResponseEntity()
    }

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
        @Parameter(description = "mock data 포함 옵션. include이면 mock data를 포함합니다.", example = "include")
        @RequestHeader(name = MockDataReadOptionResolver.HEADER_NAME, required = false)
        devData: String?,
        @RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false)
        acceptLanguage: String?,
    ): ResponseEntity<List<FixtureByLeagueResponse>> {
        val normalizedLeagueUid = leagueUid.trim()
        if (normalizedLeagueUid.isEmpty()) return ResponseEntity.badRequest().build()
        val zoneId =
            try {
                ZoneId.of(timezone.trim())
            } catch (_: Exception) {
                ZoneOffset.UTC
            }
        val normalizedDate = date?.trim()
        val localDate =
            if (normalizedDate.isNullOrEmpty()) {
                null
            } else {
                try {
                    LocalDate.parse(normalizedDate)
                } catch (_: Exception) {
                    null
                }
            }
        val atInstant = localDate?.atStartOfDay(zoneId)?.toInstant()

        val locale = localeResolver.resolve(acceptLanguage)
        return toLocalizedResponse(
            leagueAndFixtureWebService.getFixturesByLeague(
                normalizedLeagueUid,
                atInstant,
                mode,
                zoneId,
                MockDataReadOptionResolver.resolve(devData),
                locale,
            ),
            locale,
        )
    }

    private fun <T : Any> toLocalizedResponse(
        result: DomainResult<T, DomainFail>,
        locale: SupportedLocale,
    ): ResponseEntity<T> =
        when (result) {
            is DomainResult.Success ->
                ResponseEntity
                    .ok()
                    .header(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE)
                    .header(HttpHeaders.CONTENT_LANGUAGE, locale.code)
                    .body(result.value)
            is DomainResult.Fail -> ResponseEntity.status(result.error.toHttpStatus()).build()
        }
}
