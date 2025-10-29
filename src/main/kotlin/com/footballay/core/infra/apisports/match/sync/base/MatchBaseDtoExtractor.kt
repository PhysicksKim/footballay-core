package com.footballay.core.infra.apisports.match.sync.base

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.FixtureApiSportsDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.apisports.entity.VenueApiSports

interface MatchBaseDtoExtractor {
    /**
     * fixture 기본 정보
     * 예를 들어 lineup, event, playerstats, teamstats 를 제외한 경우에 대한 내용
     *
     * - [FixtureApiSports] 의 기본 필드
     * - [VenueApiSports]
     * - [LeagueApiSportsSeason]
     */
    fun extractBaseMatch(dto: FullMatchSyncDto): FixtureApiSportsDto
}
