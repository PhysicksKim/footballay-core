package com.footballay.core.web.football.controller

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// 절대로! Admin 의 로직을 재활용하면 안됩니다. 웹서비스 뿐만 아니라 도메인까지도 로직을 공유하면 안됩니다.

/**
 * Football Fixture API Controller
 *
 * 일반 유저가 경기 관련 정보를 조회
 *
 * API 버전: v1
 * Base Path: /api/v1/football/
 */
@Tag(
    name = "V1 Football - Fixture",
    description = "일반 유저가 경기 정보 조회하는 API",
)
@Validated
@RestController
@RequestMapping("/api/v1/football/")
class V1FixtureController {
    /**
     * @see [FootballStreamDataController.fixtureListByLeague]
     */
    @GetMapping("league/{leagueUid}/fixtures")
    fun getFixturesByLeague(
        @PathVariable("leagueUid")
        leagueUid: String,
        @Parameter(description = "ISO-8601 UTC")
        @RequestParam(required = false)
        at: String?,
        @Parameter(description = "nearest | exact")
        @RequestParam(required = false, defaultValue = "exact")
        @Pattern(regexp = "exact|nearest")
        mode: String, // mode 기본값 exact
    ): String {
        TODO("리그별 경기 정보를 제공합니다. 모드에 따라서 다르게 동작해야합니다.")
    }
}
