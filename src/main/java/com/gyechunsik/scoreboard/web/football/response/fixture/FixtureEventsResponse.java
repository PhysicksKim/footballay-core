package com.gyechunsik.scoreboard.web.football.response.fixture;

import jakarta.annotation.Nullable;

import java.util.List;

public record FixtureEventsResponse(
        long fixtureId,
        List<_Events> events
) {

    public record _Events(
            int sequence,
            int elapsed,
            int extraTime,
            _Team team,
            @Nullable _Player player,
            @Nullable _Player assist,
            String type, // subst, Goal, Card, Var
            String detail, // Yellow Card, Red Card, Substitution 1 2 3 ...
            String comments
    ) {
    }

    public record _Player(
            @Nullable Long playerId,
            @Nullable String name,
            @Nullable String koreanName,
            @Nullable Integer number
    ) {
    }

    public record _Team(
            long teamId,
            String name,
            String koreanName
    ) {
    }

}
