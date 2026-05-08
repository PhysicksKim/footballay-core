package com.footballay.core.cache.matchdata.polling.refresh

import com.footballay.core.cache.matchdata.polling.MatchDataPollingCacheManager
import org.springframework.stereotype.Service

@Service
class FixtureSnapshotRefreshService(
    private val pollingCacheManager: MatchDataPollingCacheManager,
) {
    fun refreshAll(trigger: FixtureMatchCacheRefreshTrigger) {
        pollingCacheManager.refreshFixture(
            fixtureUid = trigger.fixtureUid,
            source = trigger.source,
            jobPhase = trigger.jobPhase,
        )
    }
}
