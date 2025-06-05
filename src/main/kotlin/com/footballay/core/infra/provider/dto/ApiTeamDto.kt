package com.footballay.core.infra.provider.dto

/**
 * ApiSports로부터 넘어오는 팀 정보 중 최소한으로 필요한 필드
 */
data class ApiTeamDto(
    val apiId: Long,
    val name: String,
    val code: String?,
    val country: String?,
    val founded: Int?,
    val national: Boolean = false,
    val logo: String?,
    val venueId: Long?,
    val venueName: String?,
    val venueAddress: String?,
    val venueCity: String?,
    val venueCapacity: Int?
) 