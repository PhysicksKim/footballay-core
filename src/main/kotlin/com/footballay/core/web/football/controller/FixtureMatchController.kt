package com.footballay.core.web.football.controller

import com.footballay.core.common.result.toHttpStatus
import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.common.logging.logger
import com.footballay.core.cache.matchdata.polling.hash.FixtureHttpEtagHelper
import com.footballay.core.web.football.dto.*
import com.footballay.core.web.football.service.FixtureWebResult
import com.footballay.core.web.football.service.FixtureWebService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * Football Fixture Public API Controller
 *
 * UID 기반 라이브 매치 데이터 조회 API를 제공합니다.
 *
 * API 버전: v1
 * Base Path: /api/v1/football/fixtures
 *
 * 응답 구조:
 * - 성공: 각 DTO(FixtureInfoResponse 등)를 그대로 반환
 * - 실패: 공통 DomainResult → ResponseEntity 매핑(@ControllerAdvice / toResponseEntity)에 따름
 */
@Tag(
    name = "Football - Fixtures",
    description = "UID 기반 경기 정보 / 라이브 상태 / 이벤트 / 라인업 / 통계를 조회하는 퍼블릭 API",
)
@Validated
@RestController
@RequestMapping("/api/v1/football/fixtures")
class FixtureMatchController(
    private val webService: FixtureWebService,
    private val httpEtagHelper: FixtureHttpEtagHelper,
) {
    private val log = logger()

    @Operation(
        summary = "경기 기본 정보 조회",
        description = "리그, 홈/원정 팀, 킥오프 시간, 경기장 등 기본 정보를 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "정상 조회",
            content = [Content(schema = Schema(implementation = FixtureInfoResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "Fixture를 찾을 수 없음"),
        ApiResponse(responseCode = "400", description = "잘못된 UID 형식"),
    )
    @GetMapping("/{uid}/info")
    fun getFixtureInfo(
        @Parameter(description = "Fixture UID (예: yp4nn06fntg591kk)")
        @PathVariable
        @NotBlank uid: String,
    ): ResponseEntity<FixtureInfoResponse> {
        log.info("GET /api/v1/football/fixtures/{}/info", uid)
        return webService
            .getFixtureInfo(uid)
            .toResponseEntity()
    }

    @Operation(
        summary = "경기 라이브 상태 조회",
        description = "스코어, 경기 시간, 진행 상태(전반, 후반, 종료 등)를 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(implementation = FixtureLiveStatusResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "Fixture를 찾을 수 없음"),
    )
    @GetMapping("/{uid}/status")
    fun getFixtureLiveStatus(
        @Parameter(description = "Fixture UID (예: yp4nn06fntg591kk)")
        @PathVariable
        @NotBlank uid: String,
        @RequestHeader(name = HttpHeaders.IF_NONE_MATCH, required = false)
        ifNoneMatch: String?,
        @RequestHeader(name = FIXTURE_CACHE_CONTROL_HEADER, required = false)
        fixtureCacheControl: String?,
    ): ResponseEntity<String> {
        log.info("GET /api/v1/football/fixtures/{}/status", uid)
        return toPollingResponse(webService.getFixtureLiveStatus(uid, ifNoneMatch, shouldBypassCacheRead(fixtureCacheControl)))
    }

    @Operation(
        summary = "경기 라인업 조회",
        description = "홈/원정 선발/교체 선수 라인업 정보를 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(implementation = FixtureLineupResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "Fixture를 찾을 수 없음"),
    )
    @GetMapping("/{uid}/lineup")
    fun getFixtureLineup(
        @Parameter(description = "Fixture UID (예: yp4nn06fntg591kk)")
        @PathVariable
        @NotBlank uid: String,
        @RequestHeader(name = HttpHeaders.IF_NONE_MATCH, required = false)
        ifNoneMatch: String?,
        @RequestHeader(name = FIXTURE_CACHE_CONTROL_HEADER, required = false)
        fixtureCacheControl: String?,
    ): ResponseEntity<String> {
        log.info("GET /api/v1/football/fixtures/{}/lineup", uid)
        return toPollingResponse(webService.getFixtureLineup(uid, ifNoneMatch, shouldBypassCacheRead(fixtureCacheControl)))
    }

    @Operation(
        summary = "경기 이벤트 조회",
        description = "골, 카드, 교체 등 이벤트 타임라인을 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(implementation = FixtureEventsResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "Fixture를 찾을 수 없음"),
    )
    @GetMapping("/{uid}/events")
    fun getFixtureEvents(
        @Parameter(description = "Fixture UID (예: yp4nn06fntg591kk)")
        @PathVariable
        @NotBlank uid: String,
        @RequestHeader(name = HttpHeaders.IF_NONE_MATCH, required = false)
        ifNoneMatch: String?,
        @RequestHeader(name = FIXTURE_CACHE_CONTROL_HEADER, required = false)
        fixtureCacheControl: String?,
    ): ResponseEntity<String> {
        log.info("GET /api/v1/football/fixtures/{}/events", uid)
        return toPollingResponse(webService.getFixtureEvents(uid, ifNoneMatch, shouldBypassCacheRead(fixtureCacheControl)))
    }

    @Operation(
        summary = "경기 통계 조회",
        description = "팀/선수별 경기 통계를 조회합니다.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            content = [Content(schema = Schema(implementation = FixtureStatisticsResponse::class))],
        ),
        ApiResponse(responseCode = "404", description = "Fixture를 찾을 수 없음"),
    )
    @GetMapping("/{uid}/statistics")
    fun getFixtureStatistics(
        @Parameter(description = "Fixture UID (예: yp4nn06fntg591kk)")
        @PathVariable
        @NotBlank uid: String,
        @RequestHeader(name = HttpHeaders.IF_NONE_MATCH, required = false)
        ifNoneMatch: String?,
        @RequestHeader(name = FIXTURE_CACHE_CONTROL_HEADER, required = false)
        fixtureCacheControl: String?,
    ): ResponseEntity<String> {
        log.info("GET /api/v1/football/fixtures/{}/statistics", uid)
        return toPollingResponse(webService.getFixtureStatistics(uid, ifNoneMatch, shouldBypassCacheRead(fixtureCacheControl)))
    }

    private fun shouldBypassCacheRead(fixtureCacheControl: String?): Boolean = fixtureCacheControl.equals(CACHE_CONTROL_BYPASS, ignoreCase = true)

    private fun toPollingResponse(result: FixtureWebResult): ResponseEntity<String> =
        when (result) {
            is FixtureWebResult.Ok ->
                ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .eTag(httpEtagHelper.toWeakEtag(result.etagHash))
                    .body(result.snapshotJson)
            is FixtureWebResult.NotModified ->
                ResponseEntity
                    .status(304)
                    .eTag(httpEtagHelper.toWeakEtag(result.etagHash))
                    .build()
            is FixtureWebResult.Fail ->
                ResponseEntity
                    .status(result.error.toHttpStatus())
                    .build()
        }

    private companion object {
        const val FIXTURE_CACHE_CONTROL_HEADER = "X-Fixture-Cache-Control"
        const val CACHE_CONTROL_BYPASS = "bypass"
    }
}
