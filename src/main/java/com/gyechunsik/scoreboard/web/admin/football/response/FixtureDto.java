package com.gyechunsik.scoreboard.web.admin.football.response;

import java.time.ZonedDateTime;

public record FixtureDto(
        long fixtureId,
        String referee,
        String timezone,
        ZonedDateTime date,
        long timestamp,
        LiveStatusDto liveStatus,
        LeagueDto league,
        TeamDto homeTeam,
        TeamDto awayTeam
) {
}
