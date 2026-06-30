package com.footballay.core.infra.apisports.match.plan.dto

data class MatchEventPlanDto(
    val events: List<MatchEventDto> = emptyList(),
)
