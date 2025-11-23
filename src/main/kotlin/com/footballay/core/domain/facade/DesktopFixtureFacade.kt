package com.footballay.core.domain.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.FixtureModel
import java.time.Instant
import java.time.ZoneId

/**
 * Desktop App용 Fixture 조회 Facade Interface
 */
interface DesktopFixtureFacade {
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
    ): DomainResult<List<FixtureModel>, DomainFail>
}
