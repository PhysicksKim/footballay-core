package com.footballay.core.infra.apisports.match.sync.dto

data class MatchTeamDto(
    val teamApiId: Long,
    val formation: String?,
    val teamApiSportsInfo: TeamApiSportsDto
)