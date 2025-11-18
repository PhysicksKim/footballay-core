package com.footballay.core.infra.apisports.match.sync.dto

data class MatchEventPlanDto(
    val events: List<MatchEventDto> = emptyList(),
)
