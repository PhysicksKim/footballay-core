package com.footballay.core.web.admin.apisports.dto

import com.footballay.core.domain.league.MatchCollect

data class AvailableLeagueDto(
    val photo: String? = null,
    val uid: String,
    val name: String,
    val matchCollect: MatchCollect,
    val apiSports: LeagueApiSportsDto,
) {
    data class LeagueApiSportsDto(
        val apiId: Long,
    )
}
