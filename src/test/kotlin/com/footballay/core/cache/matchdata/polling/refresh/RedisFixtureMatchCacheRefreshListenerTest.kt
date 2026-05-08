package com.footballay.core.cache.matchdata.polling.refresh

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.DefaultMessage
import java.time.Instant

class RedisFixtureMatchCacheRefreshListenerTest {
    private lateinit var objectMapper: ObjectMapper
    private lateinit var refreshUseCase: FixtureMatchCacheRefreshUseCase
    private lateinit var listener: RedisFixtureMatchCacheRefreshListener

    @BeforeEach
    fun setUp() {
        objectMapper =
            ObjectMapper()
                .registerModule(KotlinModule.Builder().build())
                .registerModule(JavaTimeModule())

        refreshUseCase = mockk()
        listener = RedisFixtureMatchCacheRefreshListener(objectMapper, refreshUseCase)
    }

    @Test
    fun `onMessage - valid payload 면 refresh use case 를 호출한다`() {
        val trigger =
            FixtureMatchCacheRefreshTrigger(
                fixtureUid = "fixture-1",
                occurredAt = Instant.parse("2026-04-24T00:00:00Z"),
                source = "MATCH_DATA_SYNC",
                jobPhase = "LIVE_MATCH",
            )
        every { refreshUseCase.handle(trigger) } just runs

        listener.onMessage(
            DefaultMessage(
                FixtureMatchCacheRefreshChannels.FIXTURE_CACHE_REFRESH.toByteArray(),
                objectMapper.writeValueAsBytes(trigger),
            ),
            null,
        )

        verify { refreshUseCase.handle(trigger) }
    }
}
