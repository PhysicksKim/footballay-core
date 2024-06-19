package com.gyechunsik.scoreboard.web.admin.football.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

public record AvailableFixtureDto(
        long fixtureId,
        String referee,
        String timezone,
        ZonedDateTime date,
        Long timestamp,
        boolean available,
        LiveStatusDto liveStatus,
        LeagueDto league,
        TeamDto homeTeam,
        TeamDto awayTeam
        ) {
}
