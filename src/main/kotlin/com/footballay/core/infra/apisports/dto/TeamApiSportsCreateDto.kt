package com.footballay.core.infra.apisports.dto

data class TeamApiSportsCreateDto(
    val apiId: Long,
    val name: String,
    val code: String?,
    val country: String?,
    val founded: Int?,
    val national: Boolean?,
    val logo: String?,
    val venue: VenueApiSportsCreateDto? = null, // API 응답의 team.venue
)

data class VenueApiSportsCreateDto(
    val apiId: Long,
    val name: String,
    val address: String?,
    val city: String?,
    val capacity: Int?,
    val surface: String?,
    val image: String?,
)