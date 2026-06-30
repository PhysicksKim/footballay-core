package com.footballay.core.infra.apisports.match.plan.loader

import com.footballay.core.infra.apisports.match.plan.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext

interface MatchDataLoader {
    fun loadContext(
        fixtureApiId: Long,
        context: MatchPlayerContext,
        entityBundle: MatchEntityBundle,
    )
}
