package com.footballay.core.infra.apisports.live

import com.footballay.core.infra.apisports.syncer.ApiSportsNewPlayerSync
import org.springframework.stereotype.Component

@Component
class PlayerSyncExecutor(
    private val apiSportsNewPlayerSync: ApiSportsNewPlayerSync
) {

    fun syncPlayers(dtos: List<LineupPlayerSyncRequest>) {
        dtos.forEach { apiSportsNewPlayerSync.syncPlayers(it.dtos, it.teamId) }
    }

}