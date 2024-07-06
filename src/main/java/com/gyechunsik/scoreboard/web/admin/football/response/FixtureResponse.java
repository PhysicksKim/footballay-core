package com.gyechunsik.scoreboard.web.admin.football.response;

import java.time.ZonedDateTime;

public record FixtureResponse(
        long fixtureId,
        String referee,
        String timezone,
        ZonedDateTime date,
        long timestamp,
        LiveStatusResponse liveStatus,
        LeagueResponse league,
        TeamResponse homeTeam,
        TeamResponse awayTeam
) {
}
