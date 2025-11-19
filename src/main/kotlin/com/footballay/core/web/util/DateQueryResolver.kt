package com.footballay.core.web.util

import java.time.*

object DateQueryResolver {
    /**
     * 주어진 Instant의 `Pair<당일시작,다음날시작>`를 반환합니다.
     *
     * @param at 기준 시각. null일 경우 현재 시각 사용
     * @param clock 테스트를 위한 Clock 주입
     * @param zoneId 날짜 계산 기준 타임존. 기본값 UTC
     * @return Pair<당일시작 Instant, 다음날시작 Instant>
     */
    fun resolveExactRangeAt(
        at: Instant?,
        // 테스트를 용이하게 하기 위해서 clock 주입
        clock: Clock = Clock.systemUTC(),
        zoneId: ZoneId = ZoneOffset.UTC,
    ): Pair<Instant, Instant> {
        val base = at ?: Instant.now(clock)
        val date = base.atZone(zoneId).toLocalDate()
        val start = date.atStartOfDay(zoneId).toInstant()
        val end =
            date
                .plusDays(1)
                .atStartOfDay(zoneId)
                .toInstant()
        return start to end
    }
}
