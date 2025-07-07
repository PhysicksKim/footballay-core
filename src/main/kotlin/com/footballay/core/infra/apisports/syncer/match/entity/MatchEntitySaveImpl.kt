package com.footballay.core.infra.apisports.syncer.match.entity

import com.footballay.core.infra.apisports.live.MatchEntitySave
import com.footballay.core.infra.apisports.syncer.match.context.MatchEntityBundle
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import org.springframework.stereotype.Component

@Component
class MatchEntitySaveImpl : MatchEntitySave {
    override fun saveAllMatchEntities(
        entityBundle: MatchEntityBundle,
        context: MatchPlayerContext
    ) {
        TODO()
    }
}