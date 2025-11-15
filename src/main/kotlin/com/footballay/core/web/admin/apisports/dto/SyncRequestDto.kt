package com.footballay.core.web.admin.apisports.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class LeagueSeasonRequest(
    @field:Min(1900)
    @field:Max(2100)
    val season: Int?,
)
