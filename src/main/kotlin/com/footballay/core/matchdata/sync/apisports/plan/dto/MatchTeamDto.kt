package com.footballay.core.matchdata.sync.apisports.plan.dto

data class MatchTeamDto(
    val teamApiId: Long,
    val formation: String?,
    val teamApiSportsInfo: TeamApiSportsDto,
)
