package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.web.football.dto.AvailableLeagueResponse
import com.footballay.core.web.football.dto.FixtureByLeagueResponse
import java.time.Instant
import java.time.ZoneId

/**
 * Desktop App용 League 및 Fixture 조회 WebService Interface
 */
interface LeagueAndFixtureWebService {
    /**
     * Available한 모든 리그를 조회합니다.
     */
    fun getAvailableLeagues(): DomainResult<List<AvailableLeagueResponse>, DomainFail>

    /**
     * 리그의 경기 일정을 모드에 따라 조회합니다.
     *
     * @param leagueUid 리그 UID
     * @param at 기준 시각 (null이면 현재 시각)
     * @param mode "previous" | "exact" | "nearest"
     * @param zoneId 날짜 계산 기준 타임존
     * @return 경기 목록
     */
    fun getFixturesByLeague(
        leagueUid: String,
        at: Instant?,
        mode: String,
        zoneId: ZoneId,
    ): DomainResult<List<FixtureByLeagueResponse>, DomainFail>
}
