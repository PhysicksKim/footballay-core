package com.footballay.core.web.admin.common.util

import java.time.*

object DateQueryResolver {
    fun resolveExactRange(
        date: LocalDate?,
        clock: Clock = Clock.systemUTC(),
    ): Pair<Instant, Instant> {
        val target = date ?: LocalDate.now(clock)
        val start = target.atStartOfDay(ZoneOffset.UTC).toInstant()
        val end =
            target
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .minusNanos(1)
        return start to end
    }

    fun resolveExactRangeAt(
        at: Instant?,
        clock: Clock = Clock.systemUTC(),
    ): Pair<Instant, Instant> {
        val base = at ?: Instant.now(clock)
        val date = LocalDateTime.ofInstant(base, ZoneOffset.UTC).toLocalDate()
        val start = date.atStartOfDay(ZoneOffset.UTC).toInstant()
        val end =
            date
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .minusNanos(1)
        return start to end
    }
}
