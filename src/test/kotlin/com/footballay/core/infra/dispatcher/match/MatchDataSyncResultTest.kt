package com.footballay.core.infra.dispatcher.match

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

/**
 * MatchDataSyncResult sealed class 테스트
 *
 * Pre/Live/Post 각 단계별 Result가 올바르게 생성되고
 * 필요한 정보를 제공하는지 검증합니다.
 */
class MatchDataSyncResultTest {
    @Test
    fun `PreMatch Result 생성 및 필드 검증`() {
        // Given
        val kickoffTime = OffsetDateTime.now().plusHours(2)

        // When
        val result =
            MatchDataSyncResult.PreMatch(
                lineupCached = true,
                kickoffTime = kickoffTime,
                readyForLive = false,
            )

        // Then
        assertThat(result).isInstanceOf(MatchDataSyncResult.PreMatch::class.java)
        assertThat(result.lineupCached).isTrue()
        assertThat(result.kickoffTime).isEqualTo(kickoffTime)
        assertThat(result.readyForLive).isFalse()
    }

    @Test
    fun `PreMatch Result - readyForLive가 true인 경우`() {
        // Given
        val kickoffTime = OffsetDateTime.now().plusMinutes(3)

        // When
        val result =
            MatchDataSyncResult.PreMatch(
                lineupCached = true,
                kickoffTime = kickoffTime,
                readyForLive = true,
            )

        // Then
        assertThat(result.readyForLive).isTrue()
        assertThat(result.lineupCached).isTrue()
    }

    @Test
    fun `Live Result 생성 및 필드 검증`() {
        // Given
        val kickoffTime = OffsetDateTime.now().minusMinutes(30)

        // When
        val result =
            MatchDataSyncResult.Live(
                kickoffTime = kickoffTime,
                isMatchFinished = false,
                elapsedMin = 30,
                statusShort = "1H",
            )

        // Then
        assertThat(result).isInstanceOf(MatchDataSyncResult.Live::class.java)
        assertThat(result.kickoffTime).isEqualTo(kickoffTime)
        assertThat(result.isMatchFinished).isFalse()
        assertThat(result.elapsedMin).isEqualTo(30)
        assertThat(result.statusShort).isEqualTo("1H")
    }

    @Test
    fun `Live Result - 경기 종료 상태`() {
        // Given
        val kickoffTime = OffsetDateTime.now().minusMinutes(100)

        // When
        val result =
            MatchDataSyncResult.Live(
                kickoffTime = kickoffTime,
                isMatchFinished = true,
                elapsedMin = 90,
                statusShort = "FT",
            )

        // Then
        assertThat(result.isMatchFinished).isTrue()
        assertThat(result.statusShort).isEqualTo("FT")
    }

    @Test
    fun `PostMatch Result 생성 및 필드 검증`() {
        // Given
        val kickoffTime = OffsetDateTime.now().minusHours(2)

        // When
        val result =
            MatchDataSyncResult.PostMatch(
                kickoffTime = kickoffTime,
                shouldStopPolling = false,
                minutesSinceFinish = 30,
            )

        // Then
        assertThat(result).isInstanceOf(MatchDataSyncResult.PostMatch::class.java)
        assertThat(result.kickoffTime).isEqualTo(kickoffTime)
        assertThat(result.shouldStopPolling).isFalse()
        assertThat(result.minutesSinceFinish).isEqualTo(30)
    }

    @Test
    fun `PostMatch Result - polling 중단 조건`() {
        // Given
        val kickoffTime = OffsetDateTime.now().minusHours(3)

        // When
        val result =
            MatchDataSyncResult.PostMatch(
                kickoffTime = kickoffTime,
                shouldStopPolling = true,
                minutesSinceFinish = 70,
            )

        // Then
        assertThat(result.shouldStopPolling).isTrue()
        assertThat(result.minutesSinceFinish).isGreaterThan(60)
    }

    @Test
    fun `Error Result 생성 및 필드 검증`() {
        // Given
        val kickoffTime = OffsetDateTime.now()
        val errorMessage = "API call failed"

        // When
        val result =
            MatchDataSyncResult.Error(
                message = errorMessage,
                kickoffTime = kickoffTime,
            )

        // Then
        assertThat(result).isInstanceOf(MatchDataSyncResult.Error::class.java)
        assertThat(result.message).isEqualTo(errorMessage)
        assertThat(result.kickoffTime).isEqualTo(kickoffTime)
    }

    @Test
    fun `sealed class when 표현식으로 분기 가능`() {
        // Given
        val results =
            listOf<MatchDataSyncResult>(
                MatchDataSyncResult.PreMatch(true, OffsetDateTime.now(), false),
                MatchDataSyncResult.Live(OffsetDateTime.now(), false, 30, "1H"),
                MatchDataSyncResult.PostMatch(OffsetDateTime.now(), false, 30),
                MatchDataSyncResult.Error("Error", OffsetDateTime.now()),
            )

        // When & Then
        results.forEach { result ->
            when (result) {
                is MatchDataSyncResult.PreMatch -> assertThat(result.lineupCached).isTrue()
                is MatchDataSyncResult.Live -> assertThat(result.statusShort).isEqualTo("1H")
                is MatchDataSyncResult.PostMatch -> assertThat(result.shouldStopPolling).isFalse()
                is MatchDataSyncResult.Error -> assertThat(result.message).isEqualTo("Error")
            }
        }
    }
}

