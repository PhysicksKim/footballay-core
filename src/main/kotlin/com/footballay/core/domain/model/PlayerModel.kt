package com.footballay.core.domain.model

sealed interface PlayerExtension

/**
 * 선수 도메인 모델
 *
 * ApiSports provider의 선수 데이터를 도메인 계층에서 표현하는 모델입니다.
 * 웹 응답 객체(PlayerAdminResponse)와 분리되어 도메인 로직에 집중합니다.
 */
data class PlayerModel(
    val uid: String,
    val name: String,
    val nameKo: String?,
    val photo: String?,
    val position: String?,
    val number: Int?,
    val extension: PlayerExtension = NoPlayerExtension,
)

object NoPlayerExtension : PlayerExtension

data class PlayerApiSportsExtension(
    val apiId: Long,
    val nationality: String?,
) : PlayerExtension
