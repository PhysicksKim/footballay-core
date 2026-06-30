package com.footballay.core.infra.query

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.match.FixtureEventsModel
import com.footballay.core.domain.model.match.FixtureInfoModel
import com.footballay.core.domain.model.match.FixtureLineupModel
import com.footballay.core.domain.model.match.FixtureLiveStatusModel
import com.footballay.core.domain.model.match.FixtureStatisticsModel

interface MatchDataQueryService {
    fun getFixtureInfo(fixtureUid: String): DomainResult<FixtureInfoModel, DomainFail>

    fun getFixtureLiveStatus(fixtureUid: String): DomainResult<FixtureLiveStatusModel, DomainFail>

    fun getFixtureEvents(fixtureUid: String): DomainResult<FixtureEventsModel, DomainFail>

    fun getFixtureLineup(fixtureUid: String): DomainResult<FixtureLineupModel, DomainFail>

    fun getFixtureStatistics(fixtureUid: String): DomainResult<FixtureStatisticsModel, DomainFail>
}
