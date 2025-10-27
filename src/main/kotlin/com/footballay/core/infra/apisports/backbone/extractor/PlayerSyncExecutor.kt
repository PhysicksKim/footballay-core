package com.footballay.core.infra.apisports.backbone.extractor

import com.footballay.core.infra.apisports.backbone.sync.player.PlayerApiSportsSyncer
import com.footballay.core.infra.apisports.backbone.sync.PlayerApiSportsCreateDto
import org.springframework.stereotype.Component

@Component
class PlayerSyncExecutor(
    private val playerApiSportsSyncer: PlayerApiSportsSyncer
){

    fun syncPlayersByTeam(playersByTeam: Map<Long, List<PlayerApiSportsCreateDto>>) {
        playersByTeam.forEach { (teamId, dtos) -> playerApiSportsSyncer.syncPlayersOfTeam(teamId, dtos) }
    }

}