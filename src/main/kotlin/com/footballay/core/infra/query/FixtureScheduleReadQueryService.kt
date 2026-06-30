package com.footballay.core.infra.query

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.MockDataReadOption
import com.footballay.core.domain.model.FixtureModel
import java.time.Instant
import java.time.ZoneId

interface FixtureScheduleReadQueryService {
    fun findFixturesByLeague(
        leagueUid: String,
        at: Instant?,
        mode: String,
        zoneId: ZoneId,
        option: MockDataReadOption = MockDataReadOption.DEFAULT,
    ): DomainResult<List<FixtureModel>, DomainFail>
}
