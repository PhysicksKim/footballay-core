package com.gyechunsik.scoreboard.domain.football.dto;

public record PlayerDto(
        long id,
        String name,
        String koreanName,
        String photoUrl,
        String position
) {
}
