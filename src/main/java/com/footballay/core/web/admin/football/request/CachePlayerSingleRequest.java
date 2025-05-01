package com.footballay.core.web.admin.football.request;

public record CachePlayerSingleRequest(
        Long playerId,
        Long leagueId,
        Integer season
) {
}
