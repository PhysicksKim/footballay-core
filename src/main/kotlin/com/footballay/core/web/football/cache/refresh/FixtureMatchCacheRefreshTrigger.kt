package com.footballay.core.web.football.cache.refresh

import java.time.Instant

data class FixtureMatchCacheRefreshTrigger(
    val fixtureUid: String,
    val occurredAt: Instant = Instant.now(),
    val source: String? = null,
    val jobPhase: String? = null,
)
