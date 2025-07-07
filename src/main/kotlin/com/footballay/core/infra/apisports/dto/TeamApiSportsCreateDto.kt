package com.footballay.core.infra.apisports.dto

data class TeamApiSportsCreateDto(
    val apiId: Long,
    val name: String,
    val code: String? = null,
    val country: String? = null,
    val founded: Int? = null,
    val national: Boolean? = null,
    val logo: String? = null,
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