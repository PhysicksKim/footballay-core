package com.footballay.core.web.util

import java.time.*

object DateQueryResolver {
    /**
     * 주어진 Instant의 `Pair<당일시작,다음날시작>`를 반환합니다.
     *
     * @param at 기준 시각. null일 경우 현재 시각 사용
     * @param clock 테스트를 위한 Clock 주입
     * @return Pair<당일시작 Instant, 다음날시작 Instant>
     */
    fun resolveExactRangeAt(
        at: Instant?,
        // 테스트를 용이하게 하기 위해서 clock 주입
        clock: Clock = Clock.systemUTC(),
    ): Pair<Instant, Instant> {
        val base = at ?: Instant.now(clock)
        val date = base.atZone(ZoneOffset.UTC).toLocalDate()
        val start = date.atStartOfDay(ZoneOffset.UTC).toInstant()
        val end =
            date
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
        return start to end
    }
}
