package com.footballay.core.infra.apisports.match.live.deprecated

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext

interface MatchEntitySave {
    fun saveAllMatchEntities(entityBundle: MatchEntityBundle, context: MatchPlayerContext)
}