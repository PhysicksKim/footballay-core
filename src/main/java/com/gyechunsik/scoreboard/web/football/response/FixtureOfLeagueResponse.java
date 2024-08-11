package com.gyechunsik.scoreboard.web.football.response;

public record FixtureOfLeagueResponse(
        long fixtureId,
        _Match matchSchedule,
        _Team teamALogo,
        _Team teamBLogo,
        _Status status,
        boolean available
) {
    public record _Match(
            String kickoff,
            String round
    ) {}

    public record _Team(
            String name,
            String logo,
            String koreanName
    ) {}

    public record _Status(
            String longStatus,
            String shortStatus,
            Integer elapsed,
            _Score score
    ) {}

    public record _Score (
            Integer home,
            Integer away
    ) {}
}