package com.footballay.core.web.football.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// 절대로! Admin 의 로직을 재활용하면 안됩니다. 웹서비스 뿐만 아니라 도메인까지도 로직을 공유하면 안됩니다.

/**
 * Football League API Controller
 *
 * 일반 유저가 리그 관련 정보를 조회
 *
 * API 버전: v1
 * Base Path: /api/v1/football/
 */
@Tag(
    name = "V1 Football - Leagues",
    description = "일반 유저가 리그 조회하는 API",
)
@Validated
@RestController
@RequestMapping("/api/v1/football/")
class V1LeagueController {
    /**
     * @see [FootballStreamDataController.leagueList]
     */
    @GetMapping("leagues/available")
    fun getAvailableLeagues(): String {
        TODO("Available 리그 정보를 제공합니다")
    }
}
