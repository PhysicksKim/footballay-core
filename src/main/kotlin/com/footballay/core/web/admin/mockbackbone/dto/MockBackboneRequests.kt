package com.footballay.core.web.admin.mockbackbone.dto

import com.footballay.core.domain.fixture.FixtureStatusCode
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class MockLeagueCreateRequest(
    @field:NotBlank
    val name: String,
    val available: Boolean = true,
    val scenarioName: String? = null,
)

data class MockTeamCreateRequest(
    @field:NotBlank
    val name: String,
    val scenarioName: String? = null,
)

data class MockFixtureCreateRequest(
    @field:NotBlank
    val leagueCoreUid: String,
    val homeTeamCoreUid: String? = null,
    val awayTeamCoreUid: String? = null,
    val kickoff: Instant? = null,
    val statusCode: FixtureStatusCode = FixtureStatusCode.NS,
    val scenarioName: String? = null,
)

data class MockSimpleFixtureScenarioCreateRequest(
    @field:NotBlank
    val leagueName: String = "Mock Test League",
    @field:NotBlank
    val homeTeamName: String = "Mock Home",
    @field:NotBlank
    val awayTeamName: String = "Mock Away",
    val kickoffOffsetMinutes: Long = 10,
    val statusCode: FixtureStatusCode = FixtureStatusCode.NS,
    val leagueAvailable: Boolean = true,
    val scenarioName: String? = null,
)
