package com.gyechunsik.scoreboard.web.admin.football.response;

public record AvailableLeagueDto(long leagueId,
                                 String name,
                                 String koreanName,
                                 String logo,
                                 boolean available,
                                 int currentSeason) {
}
