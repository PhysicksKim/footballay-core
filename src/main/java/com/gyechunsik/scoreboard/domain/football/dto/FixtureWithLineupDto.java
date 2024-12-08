package com.gyechunsik.scoreboard.domain.football.dto;

import java.time.ZonedDateTime;

public record FixtureWithLineupDto(
        FixtureInfoDto fixture,
        LineupDto homeLineup,
        LineupDto awayLineup
) {
}
