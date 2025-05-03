package com.footballay.core.web.admin.football.response;

public record FixtureResponse(
        long fixtureId,
        String referee,
        String timezone,
        String date,
        long timestamp,
        LiveStatusResponse liveStatus,
        LeagueResponse league,
        TeamResponse homeTeam,
        TeamResponse awayTeam,
        boolean available
) {
}
