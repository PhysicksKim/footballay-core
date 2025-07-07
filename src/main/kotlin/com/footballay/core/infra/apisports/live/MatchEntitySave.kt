package com.footballay.core.infra.apisports.live

import com.footballay.core.infra.apisports.syncer.match.context.MatchEntityBundle
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext

interface MatchEntitySave {
    fun saveAllMatchEntities(entityBundle: MatchEntityBundle, context: MatchPlayerContext)
}