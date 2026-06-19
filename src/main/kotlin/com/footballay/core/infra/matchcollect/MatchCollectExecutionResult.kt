package com.footballay.core.infra.matchcollect

import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import java.time.Instant

sealed class MatchCollectExecutionResult {
    data class Collected(
        val fixtureUid: String,
        val status: MatchCollectStatus,
        val collectedAt: Instant,
        val syncResult: MatchDataSyncResult,
    ) : MatchCollectExecutionResult()

    data class Skipped(
        val fixtureUid: String,
        val reason: String,
    ) : MatchCollectExecutionResult()

    data class Failed(
        val fixtureUid: String,
        val message: String,
    ) : MatchCollectExecutionResult()
}
