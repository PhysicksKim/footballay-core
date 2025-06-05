package com.footballay.core.infra.provider.dto

/**
 * ApiSports로부터 넘어오는 리그 정보 중 최소한으로 필요한 필드
 */
data class ApiLeagueDto(
    val apiId: Long,
    val name: String,
    val countryName: String?,
    val countryCode: String?,
    val logo: String?,
    val currentSeason: Int
) 