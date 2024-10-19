package com.gyechunsik.scoreboard.web.football.response.fixture;

public record FixtureLiveStatusResponse(
        long fixtureId,
        _LiveStatus liveStatus
) {
    public record _LiveStatus(
            Integer elapsed,
            String shortStatus,
            String longStatus,
            _Score score
    ) {
    }

    public record _Score(
            Integer home,
            Integer away
    ) {
    }
}
