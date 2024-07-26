package com.gyechunsik.scoreboard.web.football.response;

public record FixtureOfLeagueResponse(
        long fixtureId,
        String date,
        String liveStatus,
        boolean available,
        String homeTeamName,
        String awayTeamName
) {
}
