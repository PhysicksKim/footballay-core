package com.gyechunsik.scoreboard.web.admin.football.response;

public record AvailableLeagueResponse(long leagueId,
                                      String name,
                                      String koreanName,
                                      String logo,
                                      boolean available,
                                      int currentSeason) {
}
