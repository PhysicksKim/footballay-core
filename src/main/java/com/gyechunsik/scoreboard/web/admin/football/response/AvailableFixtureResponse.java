package com.gyechunsik.scoreboard.web.admin.football.response;

import java.time.ZonedDateTime;

public record AvailableFixtureResponse(
        long fixtureId,
        String referee,
        String timezone,
        ZonedDateTime date,
        Long timestamp,
        boolean available,
        LiveStatusResponse liveStatus,
        LeagueResponse league,
        TeamResponse homeTeam,
        TeamResponse awayTeam
        ) {
}
