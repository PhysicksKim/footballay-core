package com.footballay.core.infra.apisports.live

import com.footballay.core.infra.apisports.syncer.PlayerSyncRequest

data class LineupPlayerSyncRequest (
    val teamId: Long,
    val dtos: List<PlayerSyncRequest>,
)