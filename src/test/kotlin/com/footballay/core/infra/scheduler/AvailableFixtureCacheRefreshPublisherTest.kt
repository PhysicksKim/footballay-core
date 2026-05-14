package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.web.football.cache.refresh.FixtureMatchCacheRefreshTrigger
import com.footballay.core.web.football.cache.refresh.FixtureMatchCacheRefreshTriggerPublisher
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AvailableFixtureCacheRefreshPublisherTest {
    @Mock
    private lateinit var triggerPublisher: FixtureMatchCacheRefreshTriggerPublisher

    @Test
    fun `Error가 아니면 cache refresh trigger를 발행한다`() {
        val publisher = AvailableFixtureCacheRefreshPublisher(triggerPublisher)
        val triggerCaptor = argumentCaptor<FixtureMatchCacheRefreshTrigger>()

        publisher.publishIfNeeded(
            fixtureUid = "fixture-1",
            result = MatchDataSyncResult.Live(Instant.now(), false, 30, "1H"),
            phase = AvailableFixtureJobPhase.LIVE_MATCH,
        )

        verify(triggerPublisher).publish(triggerCaptor.capture())
        assertThat(triggerCaptor.firstValue.fixtureUid).isEqualTo("fixture-1")
        assertThat(triggerCaptor.firstValue.source).isEqualTo(AvailableFixtureCacheRefreshPublisher.SOURCE)
        assertThat(triggerCaptor.firstValue.jobPhase).isEqualTo(AvailableFixtureJobPhase.LIVE_MATCH.name)
    }

    @Test
    fun `Error이면 cache refresh trigger를 발행하지 않는다`() {
        val publisher = AvailableFixtureCacheRefreshPublisher(triggerPublisher)

        publisher.publishIfNeeded(
            fixtureUid = "fixture-1",
            result = MatchDataSyncResult.Error("API error", null),
            phase = AvailableFixtureJobPhase.PRE_MATCH,
        )

        verify(triggerPublisher, never()).publish(any())
    }

    @Test
    fun `발행 실패는 삼킨다`() {
        val publisher = AvailableFixtureCacheRefreshPublisher(triggerPublisher)
        doThrow(RuntimeException("redis down")).`when`(triggerPublisher).publish(any())

        assertThatCode {
            publisher.publishIfNeeded(
                fixtureUid = "fixture-1",
                result = MatchDataSyncResult.PostMatch(Instant.now(), false, 30),
                phase = AvailableFixtureJobPhase.POST_MATCH,
            )
        }.doesNotThrowAnyException()
    }
}
