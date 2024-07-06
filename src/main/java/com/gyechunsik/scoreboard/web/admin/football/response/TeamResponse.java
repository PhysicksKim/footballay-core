package com.gyechunsik.scoreboard.web.admin.football.response;

public record TeamResponse(
        long teamId,
        String name,
        String koreanName,
        String logo
) {
}
