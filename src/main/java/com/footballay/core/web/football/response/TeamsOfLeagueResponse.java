package com.footballay.core.web.football.response;

public record TeamsOfLeagueResponse(
        long id,
        String name,
        String koreanName,
        String logo
) {
}
