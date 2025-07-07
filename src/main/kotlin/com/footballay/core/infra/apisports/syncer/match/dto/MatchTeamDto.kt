package com.footballay.core.infra.apisports.syncer.match.dto

data class MatchTeamDto(
    val teamApiId: Long,
    val formation: String?,
    val teamApiSportsInfo: TeamApiSportsDto
)