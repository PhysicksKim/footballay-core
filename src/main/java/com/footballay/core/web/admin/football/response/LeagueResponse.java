package com.footballay.core.web.admin.football.response;

public record LeagueResponse(
        long leagueId,
        String name,
        String koreanName,
        String logo
) {
}
