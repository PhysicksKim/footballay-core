package com.gyechunsik.scoreboard.domain.football.dto;

public record LeagueDto(
    Long leagueId,
    String name,
    String koreanName,
    String logo,
    boolean available,
    Integer currentSeason
) {

}
