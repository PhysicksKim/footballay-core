package com.footballay.core.domain.football.external.fetch;

public record ApiStatus(
        int current,
        int minuteLimit,
        int minuteRemaining,
        int dayLimit,
        int dayRemaining,
        boolean active
) {
}
