package com.footballay.core.infra.apisports.dto

data class FixtureApiSportsCreateDto (
    var apiId: Long? = null,

    var referee: String? = null,
    var timezone: String? = null,
    var date: String? = null,
    var timestamp: Long? = null,
    var round: String? = null,

    // related entities
    var venue: VenueOfFixtureApiSportsCreateDto? = null,
    var season: SeasonOfFixtureApiSportsCreateDto? = null,

    // embedded entities
    var status: StatusOfFixtureApiSportsCreateDto? = null,
    var score: ScoreOfFixtureApiSportsCreateDto? = null
)

data class VenueOfFixtureApiSportsCreateDto (
    var apiId: Long? = null,
    var name: String? = null,
    var city: String? = null
)

data class SeasonOfFixtureApiSportsCreateDto (
    var apiId: Long? = null,
    var name: String? = null,
    var startDate: String? = null,
    var endDate: String? = null
)

data class StatusOfFixtureApiSportsCreateDto (
    var longStatus: String? = null,
    var shortStatus: String? = null,
    var elapsed: Int? = null,
    var extra: Int? = null
)

data class ScoreOfFixtureApiSportsCreateDto (
    var halftimeHome: Int? = null,
    var halftimeAway: Int? = null,
    var fulltimeHome: Int? = null,
    var fulltimeAway: Int? = null,
    var extratimeHome: Int? = null,
    var extratimeAway: Int? = null,
    var penaltyHome: Int? = null,
    var penaltyAway: Int? = null
)



