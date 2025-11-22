package com.footballay.core.web.admin.apisports.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 팀 조회 응답 DTO
 *
 * Admin API의 팀 조회 엔드포인트에서 반환하는 웹 응답 객체입니다.
 * 도메인 모델(TeamModel)과 구조는 동일하지만, 웹 계층 전용으로 분리되어
 * 도메인 필드명 변경이 API 응답 형식에 영향을 주지 않습니다.
 */
@Schema(description = "팀 조회 응답")
data class TeamApiSportsAdminResponse(
    @Schema(description = "ApiSports 팀 ID", example = "50")
    val apiId: Long,
    @Schema(description = "팀 UID", example = "df31ldpheo58;e")
    val uid: String,
    @Schema(description = "팀 이름", example = "Manchester City")
    val name: String,
    @Schema(description = "팀 이름 (한국어)", example = "맨체스터 시티")
    val nameKo: String?,
    @Schema(description = "창단 연도", example = "1880")
    val logo: String?,
    @Schema(description = "팀 코드", example = "MCI")
    val code: String?,
)
