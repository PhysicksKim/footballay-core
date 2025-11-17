package com.footballay.core.web.admin.apisports.dto

import com.footballay.core.domain.model.ProviderDetails
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 팀 조회 응답 DTO
 *
 * Admin API의 팀 조회 엔드포인트에서 반환하는 웹 응답 객체입니다.
 * 도메인 모델(TeamModel)과 구조는 동일하지만, 웹 계층 전용으로 분리되어
 * 도메인 필드명 변경이 API 응답 형식에 영향을 주지 않습니다.
 */
@Schema(description = "팀 조회 응답")
data class TeamAdminResponse(
    @Schema(description = "ApiSports 팀 ID", example = "50")
    val teamApiId: Long,
    @Schema(description = "TeamCore ID (nullable)", example = "123")
    val teamCoreId: Long?,
    @Schema(description = "팀 UID", example = "df31ldpheo58;e")
    val uid: String,
    @Schema(description = "팀 이름", example = "Manchester City")
    val name: String,
    @Schema(description = "팀 코드", example = "MCI")
    val code: String?,
    @Schema(description = "국가", example = "England")
    val country: String?,
    @Schema(description = "Provider별 상세 정보 (ApiSportsTeamDetails, SportMonksTeamDetails 등)")
    val details: ProviderDetails,
    @Schema(description = "Provider 타입", example = "ApiSports")
    val detailsType: String,
)
