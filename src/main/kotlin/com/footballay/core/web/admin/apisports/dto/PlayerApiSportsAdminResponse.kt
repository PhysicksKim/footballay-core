package com.footballay.core.web.admin.apisports.dto

import com.footballay.core.domain.model.ProviderDetails
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 선수 조회 응답 DTO
 *
 * Admin API의 선수 조회 엔드포인트에서 반환하는 웹 응답 객체입니다.
 * 도메인 모델(PlayerModel)과 구조는 동일하지만, 웹 계층 전용으로 분리되어
 * 도메인 필드명 변경이 API 응답 형식에 영향을 주지 않습니다.
 */
@Schema(description = "선수 조회 응답")
data class PlayerApiSportsAdminResponse(
    @Schema(description = "ApiSports 선수 ID", example = "1234")
    val apiId: Long,
    @Schema(description = "선수 UID", example = "wtkkw140wk4oe")
    val uid: String,
    @Schema(description = "선수 이름", example = "Kevin De Bruyne")
    val name: String,
    @Schema(description = "선수 이름 (한국어)", example = "케빈 더 브라위너")
    val nameKo: String?,
    @Schema(description = "선수 사진 URL", example = "https://example.com/photos/1234.png")
    val photo: String?,
    @Schema(description = "포지션", example = "Midfielder")
    val position: String?,
    @Schema(description = "등번호", example = "17")
    val number: Int?,
    @Schema(description = "국적", example = "Belgium")
    val nationality: String?,
)
