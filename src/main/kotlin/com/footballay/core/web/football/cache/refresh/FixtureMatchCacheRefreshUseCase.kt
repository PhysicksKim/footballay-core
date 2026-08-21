package com.footballay.core.web.football.cache.refresh

import com.footballay.core.logger
import org.springframework.stereotype.Service

@Service
class FixtureMatchCacheRefreshUseCase(
    private val fixturePollingCacheRefreshUseCase: FixturePollingCacheRefreshUseCase,
) {
    private val log = logger()

    fun handle(trigger: FixtureMatchCacheRefreshTrigger) {
        log.info(
            "Handling fixture cache refresh trigger. fixtureUid={}, source={}, jobPhase={}",
            trigger.fixtureUid,
            trigger.source,
            trigger.jobPhase,
        )
        fixturePollingCacheRefreshUseCase.refreshAll(trigger)
    }
}
