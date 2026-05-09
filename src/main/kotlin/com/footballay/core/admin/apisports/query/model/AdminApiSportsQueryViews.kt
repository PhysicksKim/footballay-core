package com.footballay.core.admin.apisports.query.model

import java.time.Instant

data class AdminApiSportsTeamView(
    val apiId: Long,
    val uid: String,
    val name: String,
    val nameKo: String?,
    val logo: String?,
    val code: String?,
)

data class AdminApiSportsPlayerView(
    val apiId: Long,
    val uid: String,
    val name: String,
    val nameKo: String?,
    val photo: String?,
    val position: String?,
    val number: Int?,
    val nationality: String?,
)

data class AdminApiSportsFixtureSummaryView(
    val apiId: Long,
    val uid: String,
    val kickoffAt: Instant,
    val home: TeamSide?,
    val away: TeamSide?,
    val status: String,
    val statusText: String,
    val available: Boolean,
) {
    data class TeamSide(
        val name: String,
        val nameKo: String?,
        val logo: String?,
    )
}
