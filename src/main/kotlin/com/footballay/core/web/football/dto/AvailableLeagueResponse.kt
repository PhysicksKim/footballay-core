package com.footballay.core.web.football.dto

/**
 * Desktop App용 가용 리그 응답 DTO
 */
data class AvailableLeagueResponse(
    val uid: String,
    val name: String,
    val nameKo: String?,
    val logo: String?,
)
