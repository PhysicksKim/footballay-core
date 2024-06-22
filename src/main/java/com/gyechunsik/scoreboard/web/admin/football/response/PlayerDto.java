package com.gyechunsik.scoreboard.web.admin.football.response;

public record PlayerDto(
        long playerId,
        String name,
        String koreanName,
        String photoUrl,
        String position
        ) {
}
