package com.footballay.core.infra.apisports.match.dto

import com.footballay.core.infra.apisports.backbone.sync.PlayerApiSportsCreateDto

data class LineupPlayerSyncRequest (
    val teamId: Long,
    val dtos: List<PlayerApiSportsCreateDto>,
)