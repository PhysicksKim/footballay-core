package com.footballay.core.web.admin.fixture.service

import com.footballay.core.web.admin.fixture.dto.FixtureSummaryDto
import java.time.Instant

interface AdminFixtureQueryWebService {
    /**
     * 리그별 픽스처 조회
     * @param leagueId LeagueCore ID
     * @param at 기준 시각(ISO-8601 UTC) 또는 null(서버 now)
     * @param mode "nearest" | "exact" (default exact)
     */
    fun findFixturesByLeague(
        leagueId: Long,
        at: Instant?,
        mode: String,
    ): List<FixtureSummaryDto>
}
