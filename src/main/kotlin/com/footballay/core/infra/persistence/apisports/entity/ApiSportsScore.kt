package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.Embeddable

@Embeddable
data class ApiSportsScore (
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