package com.footballay.core.infra.apisports.match.sync.loader

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext

interface MatchDataLoader {

    fun loadContext(
        fixtureApiId: Long,
        context: MatchPlayerContext,
        entityBundle: MatchEntityBundle
    )

}