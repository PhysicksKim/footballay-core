package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.logger
import com.footballay.core.web.football.cache.refresh.FixtureMatchCacheRefreshTrigger
import com.footballay.core.web.football.cache.refresh.FixtureMatchCacheRefreshTriggerPublisher
import org.springframework.stereotype.Component

@Component
class AvailableFixtureCacheRefreshPublisher(
    private val refreshTriggerPublisher: FixtureMatchCacheRefreshTriggerPublisher,
) {
    private val log = logger()

    fun publishIfNeeded(
        fixtureUid: String,
        result: MatchDataSyncResult,
        phase: AvailableFixtureJobPhase,
    ) {
        if (result is MatchDataSyncResult.Error) {
            return
        }

        runCatching {
            refreshTriggerPublisher.publish(
                FixtureMatchCacheRefreshTrigger(
                    fixtureUid = fixtureUid,
                    source = SOURCE,
                    jobPhase = phase.name,
                ),
            )
        }.onFailure { ex ->
            log.warn("Failed to publish fixture cache refresh trigger. fixtureUid={}", fixtureUid, ex)
        }
    }

    companion object {
        const val SOURCE = "MATCH_DATA_SYNC"
    }
}
