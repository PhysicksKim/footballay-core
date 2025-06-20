package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.Embeddable

@Embeddable
class ApiSportsScore {
    var halftimeHome: Int? = null
    var halftimeAway: Int? = null
    var fulltimeHome: Int? = null
    var fulltimeAway: Int? = null
    var extratimeHome: Int? = null
    var extratimeAway: Int? = null
    var penaltyHome: Int? = null
    var penaltyAway: Int? = null
}