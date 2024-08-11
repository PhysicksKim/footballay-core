package com.gyechunsik.scoreboard.web.football.response.fixture;

import java.util.List;

public record FixtureLineupResponse(
        long fixtureId,
        _Lineup lineup
) {

    public record _Lineup (
            _StartLineup home,
            _StartLineup away
    ) {
    }

    public record _StartLineup(
            long teamId,
            String teamName,
            String teamKoreanName,
            String formation,
            List<_StartPlayer> players,
            List<_StartPlayer> substitutes
    ) {
    }

    public record _StartPlayer(
            long id,
            String koreanName,
            String name,
            Integer number,
            String photo,
            String position,
            String grid,
            boolean substitute
    ) {
    }
}
