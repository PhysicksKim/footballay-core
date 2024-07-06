package com.gyechunsik.scoreboard.web.football.response;

public record FixtureInfoResponse(
        long fixtureId,
        String referee,
        String date,
        String liveStatus,
        long leagueId,
        String leagueName,
        String homeTeamName,
        String awayTeamName
) {
}
