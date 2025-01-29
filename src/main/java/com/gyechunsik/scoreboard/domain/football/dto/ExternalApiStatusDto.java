package com.gyechunsik.scoreboard.domain.football.dto;

public record ExternalApiStatusDto(
        int current,
        int minuteLimit,
        int minuteRemaining,
        int dayLimit,
        int dayRemaining,
        boolean active
) {
}
