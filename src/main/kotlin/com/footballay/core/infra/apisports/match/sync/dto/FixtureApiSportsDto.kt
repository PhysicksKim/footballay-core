package com.footballay.core.infra.apisports.match.sync.dto

import java.time.OffsetDateTime

data class FixtureApiSportsDto(
    val apiId: Long,
    val referee: String?,
    val timezone: String?,
    val date: OffsetDateTime?,
    val timestamp: Long?,
    val round: String?,
    val status: StatusDto? = null,
    val score: ScoreDto? = null,
    val venue: VenueDto? = null,
    val seasonYear: Int? = null,
    val homeTeam: BaseTeamDto? = null,
    val awayTeam: BaseTeamDto? = null,
) {
    data class BaseTeamDto(
        val apiId: Long,
        val name: String,
        val logo: String = "",
        val winner: Boolean? = null,
    )

    data class VenueDto(
        var apiId: Long? = null,
        var name: String? = null,
        var city: String? = null,
    )

    data class StatusDto(
        var longStatus: String? = null,
        var shortStatus: String? = null,
        var elapsed: Int? = null,
        var extra: Int? = null,
    )

    data class ScoreDto(
        var totalHome: Int? = null,
        var totalAway: Int? = null,
        var halftimeHome: Int? = null,
        var halftimeAway: Int? = null,
        var fulltimeHome: Int? = null,
        var fulltimeAway: Int? = null,
        var extratimeHome: Int? = null,
        var extratimeAway: Int? = null,
        var penaltyHome: Int? = null,
        var penaltyAway: Int? = null,
    )
}
