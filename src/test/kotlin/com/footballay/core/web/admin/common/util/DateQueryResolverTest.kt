package com.footballay.core.web.admin.common.util

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DateQueryResolverTest {
    @Test
    fun `특정 시각의 날짜 범위를 반환한다`() {
        // given
        val instant = Instant.parse("2025-10-31T15:30:00Z")

        // when
        val (start, end) = DateQueryResolver.resolveExactRangeAt(instant)

        // then
        assertThat(Instant.parse("2025-10-31T00:00:00Z")).isEqualTo(start)
        assertThat(Instant.parse("2025-11-01T00:00:00Z")).isEqualTo(end)
    }

    @Test
    fun `null 입력시 현재 시각 기준으로 범위를 반환한다`() {
        // given
        val fixedInstant = Instant.parse("2025-10-31T12:00:00Z")
        val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

        // when
        val (start, end) = DateQueryResolver.resolveExactRangeAt(null, fixedClock)

        // then
        assertThat(Instant.parse("2025-10-31T00:00:00Z")).isEqualTo(start)
        assertThat(Instant.parse("2025-11-01T00:00:00Z")).isEqualTo(end)
    }
}
