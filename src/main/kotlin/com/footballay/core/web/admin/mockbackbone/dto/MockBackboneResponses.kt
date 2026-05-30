package com.footballay.core.web.admin.mockbackbone.dto

import java.time.Instant

data class MockLeagueResponse(
    val uid: String,
    val name: String,
    val nameKo: String?,
    val photo: String?,
    val available: Boolean,
)

data class MockTeamResponse(
    val uid: String,
    val name: String,
    val nameKo: String?,
    val code: String?,
)

data class MockFixtureResponse(
    val uid: String,
    val leagueUid: String?,
    val kickoff: Instant?,
    val round: String,
    val homeTeam: TeamSide?,
    val awayTeam: TeamSide?,
    val statusText: String,
    val statusCode: String,
    val elapsed: Int?,
    val homeScore: Int?,
    val awayScore: Int?,
    val available: Boolean,
) {
    data class TeamSide(
        val uid: String,
        val name: String,
        val nameKo: String?,
        val logo: String?,
    )
}

data class MockSimpleFixtureScenarioResponse(
    val scenarioUid: String,
    val league: MockLeagueResponse,
    val homeTeam: MockTeamResponse,
    val awayTeam: MockTeamResponse,
    val fixture: MockFixtureResponse,
)
