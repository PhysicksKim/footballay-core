package com.footballay.core.backbone.apisports.extractor

import com.footballay.core.backbone.apisports.player.PlayerApiSportsCreateDto
import com.footballay.core.backbone.apisports.player.PlayerApiSportsSyncer
import org.springframework.stereotype.Component

@Component
class PlayerSyncExecutor(
    private val playerApiSportsSyncer: PlayerApiSportsSyncer,
) {
    fun syncPlayersByTeam(playersByTeam: Map<Long, List<PlayerApiSportsCreateDto>>) {
        playersByTeam.forEach { (teamId, dtos) -> playerApiSportsSyncer.syncPlayersOfTeam(teamId, dtos) }
    }
}
