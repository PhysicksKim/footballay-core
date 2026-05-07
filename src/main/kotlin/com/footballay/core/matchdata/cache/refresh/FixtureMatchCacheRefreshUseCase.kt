package com.footballay.core.matchdata.cache.refresh

import com.footballay.core.common.logging.logger
import org.springframework.stereotype.Service

@Service
class FixtureMatchCacheRefreshUseCase(
    private val fixtureSnapshotRefreshService: FixtureSnapshotRefreshService,
) {
    private val log = logger()

    fun handle(trigger: FixtureMatchCacheRefreshTrigger) {
        log.info(
            "Handling fixture cache refresh trigger. fixtureUid={}, source={}, jobPhase={}",
            trigger.fixtureUid,
            trigger.source,
            trigger.jobPhase,
        )
        fixtureSnapshotRefreshService.refreshAll(trigger)
    }
}
