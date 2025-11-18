package com.footballay.core.infra.apisports.syncer.match.persist.result

/**
 * 매치 엔티티 동기화 결과
 */
data class MatchEntitySyncResult(
    val createdCount: Int,
    val updatedCount: Int,
    val deletedCount: Int,
    val playerChanges: MatchPlayerSyncResult,
    val eventChanges: MatchEventSyncResult,
    val success: Boolean = true,
    val errorMessage: String? = null,
) {
    companion object {
        fun success(
            createdCount: Int,
            updatedCount: Int,
            deletedCount: Int,
            playerChanges: MatchPlayerSyncResult,
            eventChanges: MatchEventSyncResult,
        ): MatchEntitySyncResult =
            MatchEntitySyncResult(
                createdCount = createdCount,
                updatedCount = updatedCount,
                deletedCount = deletedCount,
                playerChanges = playerChanges,
                eventChanges = eventChanges,
                success = true,
            )

        fun failure(errorMessage: String): MatchEntitySyncResult =
            MatchEntitySyncResult(
                createdCount = 0,
                updatedCount = 0,
                deletedCount = 0,
                playerChanges = MatchPlayerSyncResult.empty(),
                eventChanges = MatchEventSyncResult.empty(),
                success = false,
                errorMessage = errorMessage,
            )
    }
}

/**
 * MatchPlayer 동기화 결과
 */
data class MatchPlayerSyncResult(
    val created: Int,
    val updated: Int,
    val deleted: Int,
    val skipped: Int = 0,
) {
    companion object {
        fun empty() = MatchPlayerSyncResult(0, 0, 0, 0)
    }
}

/**
 * MatchEvent 동기화 결과
 */
data class MatchEventSyncResult(
    val created: Int,
    val updated: Int,
    val deleted: Int,
    val skipped: Int = 0,
) {
    companion object {
        fun empty() = MatchEventSyncResult(0, 0, 0, 0)
    }
}
