package com.footballay.core.infra.backbone.mock.scenario

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.TeamModel
import java.time.Instant

data class MockSimpleFixtureScenarioCommand(
    val leagueName: String = "Mock Test League",
    val homeTeamName: String = "Mock Home",
    val awayTeamName: String = "Mock Away",
    val kickoffOffsetMinutes: Long = 10,
    val statusCode: FixtureStatusCode = FixtureStatusCode.NS,
    val leagueAvailable: Boolean = true,
    val scenarioName: String? = null,
)

data class MockSimpleFixtureScenarioResult(
    val scenarioUid: String,
    val league: LeagueModel,
    val homeTeam: TeamModel,
    val awayTeam: TeamModel,
    val fixture: FixtureModel,
) {
    val leagueCoreUid: String = league.uid
    val homeTeamCoreUid: String = homeTeam.uid
    val awayTeamCoreUid: String = awayTeam.uid
    val fixtureCoreUid: String = fixture.uid
    val kickoff: Instant? = fixture.schedule.kickoffAt
    val statusCode: FixtureModel.StatusCode = fixture.status.code
    val leagueAvailable: Boolean = league.available
    val fixtureAvailable: Boolean = fixture.available
}
