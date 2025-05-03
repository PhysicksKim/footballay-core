package com.footballay.core.web.football.response.fixture;

public record FixtureInfoResponse(
        long fixtureId,
        String referee,
        String date,
        _League league,
        _Team home,
        _Team away
) {

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

}
