package com.footballay.core.domain.model

sealed interface TeamExtension

/**
 * 팀 도메인 모델
 *
 * ApiSports provider의 팀 데이터를 도메인 계층에서 표현하는 모델입니다.
 * 웹 응답 객체(TeamAdminResponse)와 분리되어 도메인 로직에 집중합니다.
 */
data class TeamModel(
    val uid: String,
    val name: String,
    val nameKo: String?,
    val code: String?,
    val extension: TeamExtension = NoTeamExtension,
)

object NoTeamExtension : TeamExtension

data class TeamApiSportsExtension(
    val apiId: Long,
    val founded: Int?,
    val national: Boolean, // true = 국가대표팀, false = 클럽팀
    val logo: String?,
) : TeamExtension
