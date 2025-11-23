package com.footballay.core.web.football.controller

import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.logger
import com.footballay.core.web.football.dto.*
import com.footballay.core.web.football.service.FixtureWebService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
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
    ): ResponseEntity<FixtureLiveStatusResponse> {
        log.info("GET /api/v1/football/fixtures/{}/status", uid)
        return webService
            .getFixtureLiveStatus(uid)
            .toResponseEntity()
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
    ): ResponseEntity<FixtureLineupResponse> {
        log.info("GET /api/v1/football/fixtures/{}/lineup", uid)
        return webService
            .getFixtureLineup(uid)
            .toResponseEntity()
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
    ): ResponseEntity<FixtureEventsResponse> {
        log.info("GET /api/v1/football/fixtures/{}/events", uid)
        return webService
            .getFixtureEvents(uid)
            .toResponseEntity()
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
    ): ResponseEntity<FixtureStatisticsResponse> {
        log.info("GET /api/v1/football/fixtures/{}/statistics", uid)
        return webService
            .getFixtureStatistics(uid)
            .toResponseEntity()
    }
}
