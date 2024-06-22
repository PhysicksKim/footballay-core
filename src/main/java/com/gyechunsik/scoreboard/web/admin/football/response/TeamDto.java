package com.gyechunsik.scoreboard.web.admin.football.response;

public record TeamDto(
        long teamId,
        String name,
        String koreanName,
        String logo
) {
}
