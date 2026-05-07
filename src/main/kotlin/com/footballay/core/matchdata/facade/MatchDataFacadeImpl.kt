package com.footballay.core.matchdata.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.football.match.FixtureEventsModel
import com.footballay.core.domain.football.match.FixtureInfoModel
import com.footballay.core.domain.football.match.FixtureLineupModel
import com.footballay.core.domain.football.match.FixtureLiveStatusModel
import com.footballay.core.domain.football.match.FixtureStatisticsModel
import com.footballay.core.matchdata.read.MatchDataQueryService
import org.springframework.stereotype.Service

@Service
class MatchDataFacadeImpl(
    private val matchDataQueryService: MatchDataQueryService,
) : MatchDataFacade {
    override fun getFixtureInfo(fixtureUid: String): DomainResult<FixtureInfoModel, DomainFail> =
        matchDataQueryService.getFixtureInfo(fixtureUid)

    override fun getFixtureLiveStatus(fixtureUid: String): DomainResult<FixtureLiveStatusModel, DomainFail> =
        matchDataQueryService.getFixtureLiveStatus(fixtureUid)

    override fun getFixtureEvents(fixtureUid: String): DomainResult<FixtureEventsModel, DomainFail> =
        matchDataQueryService.getFixtureEvents(fixtureUid)

    override fun getFixtureLineup(fixtureUid: String): DomainResult<FixtureLineupModel, DomainFail> =
        matchDataQueryService.getFixtureLineup(fixtureUid)

    override fun getFixtureStatistics(fixtureUid: String): DomainResult<FixtureStatisticsModel, DomainFail> =
        matchDataQueryService.getFixtureStatistics(fixtureUid)
}
