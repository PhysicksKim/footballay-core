package com.footballay.core.web.admin.apisports.service

import com.footballay.core.web.admin.apisports.dto.FixtureSummaryDto
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

interface AdminFixtureQueryWebService {
    /**
     * 리그의 Fixture 리스트 조회
     * @param leagueApiId ApiSports 리그 ID
     * @param at 기준 시각(ISO-8601 UTC) 또는 null(서버 now)
     * @param mode "nearest" | "exact" (default exact)
     * @param zoneId 날짜 계산 기준 타임존 (default UTC)
     */
    fun findFixturesByLeague(
        leagueApiId: Long,
        at: Instant?,
        mode: String,
        zoneId: ZoneId = ZoneOffset.UTC,
    ): List<FixtureSummaryDto>
}
