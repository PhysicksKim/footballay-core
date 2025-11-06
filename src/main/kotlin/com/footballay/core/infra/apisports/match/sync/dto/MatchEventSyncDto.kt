package com.footballay.core.infra.apisports.match.sync.dto

data class MatchEventSyncDto(
    val events: List<MatchEventDto> = emptyList(),
)
