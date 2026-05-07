package com.footballay.core.matchdata.sync.apisports.plan.loader

import com.footballay.core.matchdata.sync.apisports.plan.context.MatchEntityBundle
import com.footballay.core.matchdata.sync.apisports.plan.context.MatchPlayerContext

interface MatchDataLoader {
    fun loadContext(
        fixtureApiId: Long,
        context: MatchPlayerContext,
        entityBundle: MatchEntityBundle,
    )
}
