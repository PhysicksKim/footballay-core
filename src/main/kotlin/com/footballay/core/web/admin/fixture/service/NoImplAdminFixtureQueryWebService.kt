package com.footballay.core.web.admin.fixture.service

// @Service removed - replaced by AdminFixtureQueryWebServiceImpl
class NoImplAdminFixtureQueryWebService : AdminFixtureQueryWebService {
    override fun findFixturesByLeague(
        leagueId: Long,
        at: java.time.Instant?,
        mode: String,
    ): List<com.footballay.core.web.admin.fixture.dto.FixtureSummaryDto> = throw NotImplementedError("No implementation for AdminFixtureQueryWebService")
}
