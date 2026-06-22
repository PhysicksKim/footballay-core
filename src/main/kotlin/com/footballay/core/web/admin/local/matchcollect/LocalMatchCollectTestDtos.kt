package com.footballay.core.web.admin.local.matchcollect

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import java.time.Instant

data class LocalMatchCollectNowRequest(
    val now: Instant? = null,
)

data class LocalFinishedScannerRunRequest(
    val now: Instant? = null,
    val batchSize: Int = 100,
)

data class LocalQuartzFireRequest(
    val jobGroup: String,
    val jobName: String,
)

data class LocalQuartzFireResponse(
    val jobGroup: String,
    val jobName: String,
    val triggered: Boolean,
    val message: String,
)

data class LocalMatchCollectDiagnosticsResponse(
    val league: LocalMatchCollectLeagueSnapshot?,
    val fixture: LocalMatchCollectFixtureSnapshot?,
    val state: LocalMatchCollectStateSnapshot?,
    val recentStates: List<LocalMatchCollectStateSnapshot>,
    val quartzJobs: List<LocalQuartzJobSnapshot>,
)

data class LocalMatchCollectLeagueSnapshot(
    val leagueCoreUid: String,
    val name: String,
    val available: Boolean,
    val matchCollect: MatchCollect,
)

data class LocalMatchCollectFixtureSnapshot(
    val fixtureUid: String,
    val leagueCoreUid: String?,
    val seasonYear: Int?,
    val currentSeason: Boolean?,
    val kickoff: Instant?,
    val statusCode: FixtureStatusCode,
    val available: Boolean,
    val apiSportsFixtureId: Long?,
)

data class LocalMatchCollectStateSnapshot(
    val fixtureUid: String,
    val matchCollectStatus: MatchCollectStatus,
    val lastCollectedAt: Instant?,
)

data class LocalQuartzJobSnapshot(
    val jobGroup: String,
    val jobName: String,
    val jobClass: String?,
    val owner: String?,
    val phase: String?,
    val leagueUid: String?,
    val fixtureUid: String?,
    val triggers: List<LocalQuartzTriggerSnapshot>,
)

data class LocalQuartzTriggerSnapshot(
    val triggerGroup: String,
    val triggerName: String,
    val triggerState: String,
    val startTime: Instant?,
    val nextFireTime: Instant?,
    val previousFireTime: Instant?,
    val repeatIntervalMillis: Long?,
    val repeatCount: Int?,
    val timesTriggered: Int?,
)

data class LocalMatchCollectExecutionResponse(
    val resultType: String,
    val fixtureUid: String,
    val status: MatchCollectStatus?,
    val collectedAt: Instant?,
    val reason: String?,
    val message: String?,
    val syncResult: LocalMatchDataSyncResultResponse?,
)

data class LocalMatchDataSyncResultResponse(
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
