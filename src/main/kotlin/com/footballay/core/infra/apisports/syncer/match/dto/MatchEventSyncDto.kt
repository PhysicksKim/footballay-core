package com.footballay.core.infra.apisports.syncer.match.dto

data class MatchEventSyncDto(
    val events: List<MatchEventDto> = emptyList()
)