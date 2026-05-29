package com.footballay.core.infra.backbone.mock.resource

import com.footballay.core.domain.fixture.FixtureStatusCode
import java.time.Instant

data class MockFixtureCreateCommand(
    val leagueCoreUid: String,
    val homeTeamCoreUid: String? = null,
    val awayTeamCoreUid: String? = null,
    val kickoff: Instant? = null,
    val statusCode: FixtureStatusCode = FixtureStatusCode.NS,
    val fixtureAvailable: Boolean = false,
    val scenarioUid: String? = null,
    val scenarioName: String? = null,
)
