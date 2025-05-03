package com.footballay.core.web.admin.football.response;

public record AvailableFixtureResponse(
        long fixtureId,
        String referee,
        String timezone,
        String date,
        Long timestamp,
        boolean available,
        LiveStatusResponse liveStatus,
        LeagueResponse league,
        TeamResponse homeTeam,
        TeamResponse awayTeam
        ) {
}
