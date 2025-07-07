package com.footballay.core.infra.apisports.syncer.match.loader

import com.footballay.core.infra.apisports.syncer.match.context.MatchEntityBundle
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext

interface MatchDataLoader {

    fun loadContext(fixtureApiId: Long, context: MatchPlayerContext, entityBundle: MatchEntityBundle)

}