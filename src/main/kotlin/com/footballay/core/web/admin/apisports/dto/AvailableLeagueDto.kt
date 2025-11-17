package com.footballay.core.web.admin.apisports.dto

data class AvailableLeagueDto(
    val photo: String? = null,
    val uid: String,
    val name: String,
    val apiSports: LeagueApiSportsDto,
) {
    data class LeagueApiSportsDto(
        val apiId: Long,
    )
}
