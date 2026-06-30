package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import org.springframework.stereotype.Component

interface MatchCollectCacheRefreshPublisher {
    fun publishIfNeeded(
        fixtureUid: String,
        result: MatchDataSyncResult,
        phase: MatchCollectLiveJobPhase,
    )
}

@Component
class NoopMatchCollectCacheRefreshPublisher : MatchCollectCacheRefreshPublisher {
    override fun publishIfNeeded(
        fixtureUid: String,
        result: MatchDataSyncResult,
        phase: MatchCollectLiveJobPhase,
    ) {
        // Intentionally empty until LIVE match collect data is exposed through user-facing cache.
    }
}
