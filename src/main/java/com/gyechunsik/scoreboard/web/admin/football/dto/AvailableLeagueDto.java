package com.gyechunsik.scoreboard.web.admin.football.dto;

import lombok.*;

public record AvailableLeagueDto(long leagueId,
                                 String name,
                                 String koreanName,
                                 String logo,
                                 boolean available,
                                 int currentSeason) {
}
