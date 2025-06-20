package com.footballay.core.web.admin.apisports.dto

/**
 * 동기화 결과를 나타내는 기본 DTO
 */
data class SyncResultDto(
    val syncedCount: Int,
    val operation: String,
    val target: String? = null
)

/**
 * 리그 동기화 결과 DTO
 */
data class LeaguesSyncResultDto(
    val syncedCount: Int,
    val message: String = "현재 리그 동기화가 완료되었습니다"
)

/**
 * 팀 동기화 결과 DTO  
 */
data class TeamsSyncResultDto(
    val syncedCount: Int,
    val leagueApiId: Long,
    val season: Int? = null,
    val message: String = "팀 동기화가 완료되었습니다"
)

/**
 * 선수 동기화 결과 DTO
 */
data class PlayersSyncResultDto(
    val syncedCount: Int,
    val teamApiId: Long,
    val message: String = "선수 동기화가 완료되었습니다"
) 