package com.gyechunsik.scoreboard.domain.football.dto;

import java.time.ZonedDateTime;

public record FixtureInfoDto(
        long fixtureId,
        String referee,
        String round,
        String timezone,
        ZonedDateTime date,
        long timestamp,
        boolean available,
        LiveStatusDto liveStatus,
        LeagueDto league,
        TeamDto homeTeam,
        TeamDto awayTeam
) {
}
