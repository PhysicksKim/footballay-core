package com.gyechunsik.scoreboard.web.admin.football.response;

public record ExternalApiStatusResponse(
        int current,
        int minuteLimit,
        int minuteRemaining,
        int dayLimit,
        int dayRemaining,
        boolean active
) {
}
