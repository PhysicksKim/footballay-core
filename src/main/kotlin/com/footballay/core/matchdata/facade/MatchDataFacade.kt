package com.footballay.core.matchdata.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.football.match.FixtureEventsModel
import com.footballay.core.domain.football.match.FixtureInfoModel
import com.footballay.core.domain.football.match.FixtureLineupModel
import com.footballay.core.domain.football.match.FixtureLiveStatusModel
import com.footballay.core.domain.football.match.FixtureStatisticsModel

/**
 * Fixture UID 기반 match data 조회 공식 진입점.
 *
 * Web, cache refresh 같은 outer adapter는 내부 query 구현체 대신 이 facade를 바라본다.
 */
interface MatchDataFacade {
    fun getFixtureInfo(fixtureUid: String): DomainResult<FixtureInfoModel, DomainFail>

    fun getFixtureLiveStatus(fixtureUid: String): DomainResult<FixtureLiveStatusModel, DomainFail>

    fun getFixtureEvents(fixtureUid: String): DomainResult<FixtureEventsModel, DomainFail>

    fun getFixtureLineup(fixtureUid: String): DomainResult<FixtureLineupModel, DomainFail>

    fun getFixtureStatistics(fixtureUid: String): DomainResult<FixtureStatisticsModel, DomainFail>
}
