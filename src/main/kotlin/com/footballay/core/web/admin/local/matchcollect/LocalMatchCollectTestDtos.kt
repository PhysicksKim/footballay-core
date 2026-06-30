package com.footballay.core.web.admin.local.matchcollect

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Local MatchCollect executor 직접 실행 시각 요청")
data class LocalMatchCollectNowRequest(
    @field:Schema(description = "테스트 기준 시각. 미지정 시 서버 현재 시각", example = "2026-06-26T03:00:00Z", nullable = true)
    val now: Instant? = null,
)

@Schema(description = "Local FINISHED scanner 1회 실행 요청")
data class LocalFinishedScannerRunRequest(
    @field:Schema(description = "scanner 기준 시각. 미지정 시 서버 현재 시각", example = "2026-06-26T03:00:00Z", nullable = true)
    val now: Instant? = null,
    @field:Schema(description = "scanner batch size. backend 에서 1..500 범위로 보정", example = "100")
    val batchSize: Int = 100,
)

@Schema(description = "Local Quartz job manual fire 요청")
data class LocalQuartzFireRequest(
    @field:Schema(description = "Quartz job group", example = "league:match:league_core_abcd1234")
    val jobGroup: String,
    @field:Schema(description = "Quartz job name", example = "matchcollect:pre:fixture_core_abcd1234")
    val jobName: String,
)

@Schema(description = "Local Quartz job manual fire 응답")
data class LocalQuartzFireResponse(
    @field:Schema(description = "Quartz job group", example = "league:match:league_core_abcd1234")
    val jobGroup: String,
    @field:Schema(description = "Quartz job name", example = "matchcollect:pre:fixture_core_abcd1234")
    val jobName: String,
    @field:Schema(description = "trigger 요청 성공 여부", example = "true")
    val triggered: Boolean,
    @field:Schema(description = "결과 메시지", example = "Quartz job trigger requested")
    val message: String,
)

@Schema(description = "Local MatchCollect diagnostics snapshot")
data class LocalMatchCollectDiagnosticsResponse(
    @field:Schema(description = "league snapshot", nullable = true)
    val league: LocalMatchCollectLeagueSnapshot?,
    @field:Schema(description = "fixture snapshot", nullable = true)
    val fixture: LocalMatchCollectFixtureSnapshot?,
    @field:Schema(description = "fixture state snapshot", nullable = true)
    val state: LocalMatchCollectStateSnapshot?,
    @field:Schema(description = "recent league scoped states")
    val recentStates: List<LocalMatchCollectStateSnapshot>,
    @field:Schema(description = "Quartz job snapshots")
    val quartzJobs: List<LocalQuartzJobSnapshot>,
)

@Schema(description = "Local MatchCollect league snapshot")
data class LocalMatchCollectLeagueSnapshot(
    @field:Schema(description = "LeagueCore UID", example = "league_core_abcd1234")
    val leagueCoreUid: String,
    @field:Schema(description = "league name", example = "Premier League")
    val name: String,
    @field:Schema(description = "league available 여부", example = "true")
    val available: Boolean,
    @field:Schema(description = "league MatchCollect 정책", allowableValues = ["NONE", "FINISHED", "LIVE"], example = "LIVE")
    val matchCollect: MatchCollect,
)

@Schema(description = "Local MatchCollect fixture snapshot")
data class LocalMatchCollectFixtureSnapshot(
    @field:Schema(description = "FixtureCore UID", example = "fixture_core_abcd1234")
    val fixtureUid: String,
    @field:Schema(description = "LeagueCore UID", example = "league_core_abcd1234", nullable = true)
    val leagueCoreUid: String?,
    @field:Schema(description = "season year", example = "2026", nullable = true)
    val seasonYear: Int?,
    @field:Schema(description = "current season 여부", example = "true", nullable = true)
    val currentSeason: Boolean?,
    @field:Schema(description = "kickoff time", example = "2026-06-26T12:00:00Z", nullable = true)
    val kickoff: Instant?,
    @field:Schema(description = "fixture status code", example = "NS")
    val statusCode: FixtureStatusCode,
    @field:Schema(description = "available fixture 여부", example = "false")
    val available: Boolean,
    @field:Schema(description = "ApiSports fixture id", example = "1208021", nullable = true)
    val apiSportsFixtureId: Long?,
)

@Schema(description = "Local MatchCollect state snapshot")
data class LocalMatchCollectStateSnapshot(
    @field:Schema(description = "FixtureCore UID", example = "fixture_core_abcd1234")
    val fixtureUid: String,
    @field:Schema(description = "MatchCollect status", example = "FAIL_END")
    val matchCollectStatus: MatchCollectStatus,
    @field:Schema(description = "last collected time", example = "2026-06-26T03:00:00Z", nullable = true)
    val lastCollectedAt: Instant?,
)

@Schema(description = "Local Quartz job snapshot")
data class LocalQuartzJobSnapshot(
    @field:Schema(description = "Quartz job group", example = "league:match:league_core_abcd1234")
    val jobGroup: String,
    @field:Schema(description = "Quartz job name", example = "matchcollect:pre:fixture_core_abcd1234")
    val jobName: String,
    @field:Schema(description = "Quartz job class", nullable = true)
    val jobClass: String?,
    @field:Schema(description = "parsed owner", example = "MATCHCOLLECT", nullable = true)
    val owner: String?,
    @field:Schema(description = "parsed phase", example = "PRE", nullable = true)
    val phase: String?,
    @field:Schema(description = "parsed LeagueCore UID", example = "league_core_abcd1234", nullable = true)
    val leagueUid: String?,
    @field:Schema(description = "parsed FixtureCore UID", example = "fixture_core_abcd1234", nullable = true)
    val fixtureUid: String?,
    @field:Schema(description = "Quartz triggers")
    val triggers: List<LocalQuartzTriggerSnapshot>,
)

@Schema(description = "Local Quartz trigger snapshot")
data class LocalQuartzTriggerSnapshot(
    @field:Schema(description = "Quartz trigger group")
    val triggerGroup: String,
    @field:Schema(description = "Quartz trigger name")
    val triggerName: String,
    @field:Schema(description = "Quartz trigger state", example = "NORMAL")
    val triggerState: String,
    @field:Schema(description = "start fire time", nullable = true)
    val startTime: Instant?,
    @field:Schema(description = "next fire time", nullable = true)
    val nextFireTime: Instant?,
    @field:Schema(description = "previous fire time", nullable = true)
    val previousFireTime: Instant?,
    @field:Schema(description = "simple trigger repeat interval millis", example = "60000", nullable = true)
    val repeatIntervalMillis: Long?,
    @field:Schema(description = "simple trigger repeat count", example = "-1", nullable = true)
    val repeatCount: Int?,
    @field:Schema(description = "simple trigger times triggered", example = "0", nullable = true)
    val timesTriggered: Int?,
)

@Schema(description = "Local MatchCollect executor 직접 실행 응답")
data class LocalMatchCollectExecutionResponse(
    @field:Schema(description = "실행 결과 타입", allowableValues = ["COLLECTED", "SKIPPED", "FAILED"], example = "COLLECTED")
    val resultType: String,
    @field:Schema(description = "FixtureCore UID", example = "fixture_core_abcd1234")
    val fixtureUid: String,
    @field:Schema(description = "실행 후 MatchCollect status. COLLECTED 가 아닌 경우 null", example = "EARLY_SYNCED", nullable = true)
    val status: MatchCollectStatus?,
    @field:Schema(description = "수집 시각. COLLECTED 가 아닌 경우 null", example = "2026-06-26T03:00:00Z", nullable = true)
    val collectedAt: Instant?,
    @field:Schema(description = "SKIPPED 사유", nullable = true)
    val reason: String?,
    @field:Schema(description = "FAILED 메시지", nullable = true)
    val message: String?,
    @field:Schema(description = "provider sync 결과", nullable = true)
    val syncResult: LocalMatchDataSyncResultResponse?,
)

@Schema(description = "Local MatchCollect provider sync 결과")
data class LocalMatchDataSyncResultResponse(
    @field:Schema(description = "provider sync 결과 타입", allowableValues = ["PRE_MATCH", "LIVE", "POST_MATCH", "NOT_PLAYED", "ERROR"], example = "POST_MATCH")
    val resultType: String,
    @field:Schema(description = "provider 응답 기준 kickoff time", nullable = true)
    val kickoffTime: Instant?,
    @field:Schema(description = "라인업 캐시 여부. PRE_MATCH 결과에서 사용", nullable = true)
    val lineupCached: Boolean? = null,
    @field:Schema(description = "pre-match job 종료 가능 여부. PRE_MATCH 결과에서 사용", nullable = true)
    val shouldTerminatePreMatchJob: Boolean? = null,
    @field:Schema(description = "경기 진행 분. LIVE 결과에서 사용", nullable = true)
    val elapsedMin: Int? = null,
    @field:Schema(description = "fixture status code", nullable = true)
    val statusCode: FixtureStatusCode? = null,
    @field:Schema(description = "post-match polling 종료 가능 여부. POST_MATCH 결과에서 사용", nullable = true)
    val shouldStopPolling: Boolean? = null,
    @field:Schema(description = "종료 후 경과 분. POST_MATCH 결과에서 사용", nullable = true)
    val minutesSinceFinish: Long? = null,
    @field:Schema(description = "ERROR 결과 메시지", nullable = true)
    val message: String? = null,
)
