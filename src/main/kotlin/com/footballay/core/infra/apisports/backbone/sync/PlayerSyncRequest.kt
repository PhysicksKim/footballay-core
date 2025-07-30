package com.footballay.core.infra.apisports.backbone.sync

/**
 * 선수 동기화 요청 데이터
 */
data class PlayerSyncRequest(
    val apiId: Long?,
    val name: String?,
    val firstname: String? = null,
    val lastname: String? = null,
    val age: Int? = null,
    val nationality: String? = null,
    val position: String? = null,
    val number: Int? = null,
    val photo: String? = null,
    // API별 추가 필드들 확장 가능
    val additionalData: Map<String, Any> = emptyMap()
)