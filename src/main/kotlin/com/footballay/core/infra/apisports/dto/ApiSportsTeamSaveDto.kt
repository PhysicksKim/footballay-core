package com.footballay.core.infra.apisports.dto

data class ApiSportsTeamSaveDto(
    val leagueApiId: Long,
    val teamsInfo: List<ApiSportsTeamInfoDto>
)

data class ApiSportsTeamInfoDto(
    val apiId: Long,
    val name: String,
    val code: String?,
    val country: String?,
    val founded: Int?,
    val national: Boolean?,
    val logo: String?,
    val venue: ApiSportsVenueDto?
)

data class ApiSportsVenueDto(
    val apiId: Long,
    val name: String?,
    val address: String?,
    val city: String?,
    val capacity: Int?,
    val surface: String?,
    val image: String?
)