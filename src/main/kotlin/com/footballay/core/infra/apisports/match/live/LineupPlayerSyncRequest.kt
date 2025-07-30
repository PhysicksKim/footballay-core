package com.footballay.core.infra.apisports.match.live

import com.footballay.core.infra.apisports.backbone.sync.PlayerSyncRequest

data class LineupPlayerSyncRequest (
    val teamId: Long,
    val dtos: List<PlayerSyncRequest>,
)