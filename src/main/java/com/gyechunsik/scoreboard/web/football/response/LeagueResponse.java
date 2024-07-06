package com.gyechunsik.scoreboard.web.football.response;

public record LeagueResponse(long leagueId,
                             String name,
                             String koreanName,
                             String logo,
                             int currentSeason) {
}
