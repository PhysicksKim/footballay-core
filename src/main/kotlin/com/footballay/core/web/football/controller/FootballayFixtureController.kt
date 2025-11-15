package com.footballay.core.web.football.controller

import com.footballay.core.web.football.dto.*
import com.footballay.core.web.football.service.FootballayFixtureWebService
import com.footballay.core.logger
import com.footballay.core.web.common.dto.ApiResponseV2
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Footballay Fixture Public API Controller
 *
 * UID 기반 라이브 매치 데이터 조회 API를 제공합니다.
 *
 * **API 버전:** v1
 * **Base Path:** /api/v1/footballay/fixtures
 *
 * **엔드포인트:**
 * - GET /{uid}/info - 경기 기본 정보
 * - GET /{uid}/live-status - 라이브 상태 (스코어, 시간)
 * - GET /{uid}/events - 경기 이벤트 (골, 카드, 교체)
 * - GET /{uid}/lineup - 라인업 및 선수 정보
 * - GET /{uid}/statistics - 팀/선수 통계
 *
 * **응답 구조:**
 * - `{ success: boolean, data: T, error: ErrorDetail }`
 *
 * **특징:**
 * - Path variable로 fixtureUid 받음
 * - Kotlin 기반 API with ApiResponseV2
 */
@RestController
@RequestMapping("/api/v1/footballay/fixtures")
class FootballayFixtureController(
    private val webService: FootballayFixtureWebService,
) {
    private val log = logger()

    /**
     * 경기 기본 정보 조회
     *
     * @param uid Fixture UID (e.g., "apisports:1208021")
     * @return 경기 기본 정보 (리그, 팀, 날짜, 심판 등)
     */
    @GetMapping("/{uid}/info")
    fun getFixtureInfo(
        @PathVariable uid: String,
    ): ResponseEntity<ApiResponseV2<FixtureInfoDto>> {
        log.info("GET /api/v1/footballay/fixtures/{}/info", uid)
        return ResponseEntity.ok(webService.getFixtureInfo(uid))
    }

    /**
     * 경기 라이브 상태 조회
     *
     * @param uid Fixture UID
     * @return 라이브 상태 (스코어, 경기 시간, 진행 상태)
     */
    @GetMapping("/{uid}/live-status")
    fun getFixtureLiveStatus(
        @PathVariable uid: String,
    ): ResponseEntity<ApiResponseV2<FixtureLiveStatusDto>> {
        log.info("GET /api/v1/footballay/fixtures/{}/live-status", uid)
        return ResponseEntity.ok(webService.getFixtureLiveStatus(uid))
    }

    /**
     * 경기 이벤트 조회
     *
     * @param uid Fixture UID
     * @return 경기 이벤트 목록 (골, 카드, 교체 등)
     */
    @GetMapping("/{uid}/events")
    fun getFixtureEvents(
        @PathVariable uid: String,
    ): ResponseEntity<ApiResponseV2<FixtureEventsDto>> {
        log.info("GET /api/v1/footballay/fixtures/{}/events", uid)
        return ResponseEntity.ok(webService.getFixtureEvents(uid))
    }

    /**
     * 경기 라인업 조회
     *
     * @param uid Fixture UID
     * @return 라인업 정보 (홈/원정 선발/교체 선수)
     */
    @GetMapping("/{uid}/lineup")
    fun getFixtureLineup(
        @PathVariable uid: String,
    ): ResponseEntity<ApiResponseV2<FixtureLineupDto>> {
        log.info("GET /api/v1/footballay/fixtures/{}/lineup", uid)
        return ResponseEntity.ok(webService.getFixtureLineup(uid))
    }

    /**
     * 경기 통계 조회
     *
     * @param uid Fixture UID
     * @return 경기 통계 (팀/선수별 통계)
     */
    @GetMapping("/{uid}/statistics")
    fun getFixtureStatistics(
        @PathVariable uid: String,
    ): ResponseEntity<ApiResponseV2<FixtureStatisticsDto>> {
        log.info("GET /api/v1/footballay/fixtures/{}/statistics", uid)
        return ResponseEntity.ok(webService.getFixtureStatistics(uid))
    }
}
