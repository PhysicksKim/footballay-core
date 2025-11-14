package com.footballay.core.domain.model

/**
 * 팀 도메인 모델
 *
 * ApiSports provider의 팀 데이터를 도메인 계층에서 표현하는 모델입니다.
 * 웹 응답 객체(TeamAdminResponse)와 분리되어 도메인 로직에 집중합니다.
 */
data class TeamModel(
    val teamApiId: Long,
    val teamCoreId: Long?,
    val uid: String,
    val name: String,
    val code: String?,
    val country: String?,
    val details: ProviderDetails,
    val detailsType: String,
)
