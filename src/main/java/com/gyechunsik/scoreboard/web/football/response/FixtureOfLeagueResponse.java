package com.gyechunsik.scoreboard.web.football.response;

public record FixtureOfLeagueResponse(
        long fixtureId,
        String date,
        String liveStatus,
        String homeTeamName,
        String awayTeamName
) {
}
