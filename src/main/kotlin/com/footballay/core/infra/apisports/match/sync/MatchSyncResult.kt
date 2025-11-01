package com.footballay.core.infra.apisports.match.sync

/**
 * 매치 동기화 결과
 */
data class MatchSyncResult(
    val success: Boolean,
    val processedCount: Int = 0,
    val newEntitiesCount: Int = 0,
    val errorMessage: String? = null,
    val details: Map<String, Any> = emptyMap(),
) {
    companion object {
        fun success(
            processedCount: Int = 0,
            newEntitiesCount: Int = 0,
            details: Map<String, Any> = emptyMap(),
        ) = MatchSyncResult(true, processedCount, newEntitiesCount, null, details)

        fun failure(
            errorMessage: String,
            details: Map<String, Any> = emptyMap(),
        ) = MatchSyncResult(false, 0, 0, errorMessage, details)
    }
}
