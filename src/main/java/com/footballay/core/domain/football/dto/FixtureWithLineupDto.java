package com.footballay.core.domain.football.dto;

import org.jetbrains.annotations.Nullable;

public record FixtureWithLineupDto(
        FixtureInfoDto fixture,
        @Nullable LineupDto homeLineup,
        @Nullable LineupDto awayLineup
) {
}
