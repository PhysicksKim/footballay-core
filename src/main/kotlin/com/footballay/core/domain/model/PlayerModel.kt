package com.footballay.core.domain.model

/**
 * 선수 도메인 모델
 *
 * ApiSports provider의 선수 데이터를 도메인 계층에서 표현하는 모델입니다.
 * 웹 응답 객체(PlayerAdminResponse)와 분리되어 도메인 로직에 집중합니다.
 */
data class PlayerModel(
    val playerApiId: Long,
    val playerCoreId: Long?,
    val uid: String,
    val name: String,
    val firstname: String?,
    val lastname: String?,
    val position: String?,
    val number: Int?,
    val details: ProviderDetails,
    val detailsType: String,
)
