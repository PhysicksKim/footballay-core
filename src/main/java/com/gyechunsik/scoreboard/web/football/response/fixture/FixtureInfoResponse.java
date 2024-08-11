package com.gyechunsik.scoreboard.web.football.response.fixture;

import java.util.List;

public record FixtureInfoResponse(
        long fixtureId,
        String referee,
        String date,
        _League league,
        _Team home,
        _Team away
) {

    public record _Player(
            long id,
            String name,
            String koreanName,
            String photo
    ) {
    }

    public record _League(
            long id,
            String name,
            String koreanName,
            String logo
    ) {
    }

    public record _Team(
            long id,
            String name,
            String koreanName,
            String logo
    ) {
    }

    // ---------------- deprecated ----------------
    // public record _LiveStatus(
    //         Integer elapsed,
    //         String shortStatus,
    //         String longStatus
    // ) {
    // }
    //
    // public record _FixtureEventResponse(
    //         long teamId,
    //         _Player player,
    //         _Player assist,
    //         long elapsed,
    //         String type,
    //         String detail,
    //         String comments
    // ) {
    // }
    //
    // public record _Lineup (
    //         _StartLineup home,
    //         _StartLineup away
    // ) {
    // }
    //
    // public record _StartLineup(
    //         long teamId,
    //         String formation,
    //         List<_StartPlayer> players,
    //         List<_StartPlayer> substitutes
    // ) {
    // }
    //
    // public record _StartPlayer(
    //         long id,
    //         String koreanName,
    //         String name,
    //         Integer number,
    //         String photo,
    //         String position,
    //         String grid,
    //         boolean substitute
    // ) {
    // }

}
