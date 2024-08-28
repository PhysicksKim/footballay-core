package com.gyechunsik.scoreboard.web.admin.football.request;

public record CachePlayerSingleRequest(
        Long playerId,
        Long leagueId,
        Integer season
) {
}
