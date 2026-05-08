package com.footballay.core.cache.matchdata.polling.refresh

import com.footballay.core.cache.matchdata.polling.MatchDataPollingCacheManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FixtureSnapshotRefreshServiceTest {
    private lateinit var pollingCacheManager: MatchDataPollingCacheManager
    private lateinit var service: FixtureSnapshotRefreshService

    @BeforeEach
    fun setUp() {
        pollingCacheManager = mockk(relaxed = true)
        service =
            FixtureSnapshotRefreshService(
                pollingCacheManager = pollingCacheManager,
            )
    }

    @Test
    fun `refreshAll - trigger 정보를 polling cache manager 에 위임한다`() {
        val trigger =
            FixtureMatchCacheRefreshTrigger(
                fixtureUid = "fixture-1",
                source = "MATCH_DATA_SYNC",
                jobPhase = "LIVE_MATCH",
            )

        service.refreshAll(trigger)

        verify {
            pollingCacheManager.refreshFixture(
                fixtureUid = "fixture-1",
                source = "MATCH_DATA_SYNC",
                jobPhase = "LIVE_MATCH",
            )
        }
    }
}
