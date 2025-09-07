package com.footballay.core.infra.apisports.backbone.extractor

import com.footballay.core.infra.apisports.backbone.sync.ApiSportsNewPlayerSync
import com.footballay.core.infra.apisports.backbone.sync.player.PlayerApiSportsSyncer
import com.footballay.core.infra.apisports.match.dto.LineupPlayerSyncRequest
import org.springframework.stereotype.Component

@Component
class PlayerSyncExecutor(
//    private val apiSportsNewPlayerSync: ApiSportsNewPlayerSync
    private val playerApiSportsSyncer: PlayerApiSportsSyncer
) {

    fun syncPlayers(dtos: List<LineupPlayerSyncRequest>) {
        dtos.forEach { playerApiSportsSyncer.syncPlayersOfTeam(it.teamId, it.dtos) }
    }

}