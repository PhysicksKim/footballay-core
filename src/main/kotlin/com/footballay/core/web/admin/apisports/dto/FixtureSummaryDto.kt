package com.footballay.core.web.admin.apisports.dto

data class FixtureSummaryDto(
    val uid: String,
    val kickoffAt: String,
    val home: TeamDto?,
    val away: TeamDto?,
    val status: String,
    val statusText: String,
    val available: Boolean,
    val apiId: Long?,
) {
    data class TeamDto(
        val name: String,
        val nameKo: String?,
        val logo: String?,
    )
}
