package com.footballay.core.web.admin.apisports.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 동기화 결과를 나타내는 기본 DTO
 */
@Schema(description = "동기화 작업 결과")
data class SyncResultDto(
    @Schema(description = "동기화된 항목 수", example = "10")
    val syncedCount: Int,
    @Schema(description = "수행된 작업", example = "sync")
    val operation: String,
    @Schema(description = "동기화 대상", example = "leagues", nullable = true)
    val target: String? = null,
)

/**
 * 리그 동기화 결과 DTO
 */
@Schema(description = "리그 동기화 결과")
data class LeaguesSyncResultDto(
    @Schema(description = "동기화된 리그 수", example = "5")
    val syncedCount: Int,
    @Schema(description = "결과 메시지", example = "현재 리그 동기화가 완료되었습니다")
    val message: String = "현재 리그 동기화가 완료되었습니다",
)

/**
 * 팀 동기화 결과 DTO
 */
@Schema(description = "팀 동기화 결과")
data class TeamsSyncResultDto(
    @Schema(description = "동기화된 팀 수", example = "20")
    val syncedCount: Int,
    @Schema(description = "ApiSports 리그 ID", example = "39")
    val leagueApiId: Long,
    @Schema(description = "동기화한 시즌", example = "2024", nullable = true)
    val season: Int? = null,
    @Schema(description = "결과 메시지", example = "팀 동기화가 완료되었습니다")
    val message: String = "팀 동기화가 완료되었습니다",
)

/**
 * 선수 동기화 결과 DTO
 */
@Schema(description = "선수 동기화 결과")
data class PlayersSyncResultDto(
    @Schema(description = "동기화된 선수 수", example = "25")
    val syncedCount: Int,
    @Schema(description = "ApiSports 팀 ID", example = "50")
    val teamApiId: Long,
    @Schema(description = "결과 메시지", example = "선수 동기화가 완료되었습니다")
    val message: String = "선수 동기화가 완료되었습니다",
)
