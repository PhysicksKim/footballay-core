package com.footballay.core.web.admin.football.response;

public record ExternalApiStatusResponse(
        int current,
        int minuteLimit,
        int minuteRemaining,
        int dayLimit,
        int dayRemaining,
        boolean active
) {
}
