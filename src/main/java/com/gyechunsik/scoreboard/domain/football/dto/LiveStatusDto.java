package com.gyechunsik.scoreboard.domain.football.dto;

public record LiveStatusDto(
        String longStatus,
        String shortStatus,
        Integer elapsed,
        Integer homeScore,
        Integer awayScore
) {
}
