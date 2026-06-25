package com.footballay.core.web.admin.core.dto

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class MatchCollectUpdateRequest(
    @field:NotNull
    val matchCollect: MatchCollect,
)

data class MatchCollectUpdateResponse(
    val uid: String,
    val matchCollect: MatchCollect,
    val reconcileSuccess: Boolean,
)

data class AdminMatchCollectExecutionResponse(
    val resultType: String,
    val fixtureUid: String,
    val status: MatchCollectStatus?,
    val collectedAt: Instant?,
    val reason: String?,
    val message: String?,
    val syncResult: AdminMatchDataSyncResultResponse?,
)

data class AdminMatchDataSyncResultResponse(
    val resultType: String,
    val kickoffTime: Instant?,
    val lineupCached: Boolean? = null,
    val shouldTerminatePreMatchJob: Boolean? = null,
    val elapsedMin: Int? = null,
    val statusCode: FixtureStatusCode? = null,
    val shouldStopPolling: Boolean? = null,
    val minutesSinceFinish: Long? = null,
    val message: String? = null,
)
