package com.gyechunsik.scoreboard.web.football.response.fixture;

public record FixtureLiveStatusResponse(
        long fixtureId,
        _LiveStatus liveStatus
) {
    public record _LiveStatus(
            Integer elapsed,
            String shortStatus,
            String longStatus
    ) {
    }
}
