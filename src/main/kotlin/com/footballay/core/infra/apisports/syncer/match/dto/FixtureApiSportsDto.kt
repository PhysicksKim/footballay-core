package com.footballay.core.infra.apisports.syncer.match.dto

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
) {
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

    data class ScoreDto (
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


