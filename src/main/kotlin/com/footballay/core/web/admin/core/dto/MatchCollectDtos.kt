package com.footballay.core.web.admin.core.dto

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.Instant

@Schema(description = "리그 MatchCollect 정책 변경 요청")
data class MatchCollectUpdateRequest(
    @field:Schema(description = "리그 MatchCollect 정책", allowableValues = ["NONE", "FINISHED", "LIVE"], example = "FINISHED")
    @field:NotNull
    val matchCollect: MatchCollect,
)

@Schema(description = "리그 MatchCollect 정책 변경 응답")
data class MatchCollectUpdateResponse(
    @field:Schema(description = "LeagueCore UID", example = "abcd1234")
    val uid: String,
    @field:Schema(description = "변경 후 MatchCollect 정책", allowableValues = ["NONE", "FINISHED", "LIVE"], example = "FINISHED")
    val matchCollect: MatchCollect,
    @field:Schema(description = "변경 직후 Quartz reconcile 성공 여부", example = "true")
    val reconcileSuccess: Boolean,
)

@Schema(description = "운영자 단건 MatchCollect 실행 응답")
data class AdminMatchCollectExecutionResponse(
    @field:Schema(description = "실행 결과 타입", allowableValues = ["COLLECTED", "SKIPPED", "FAILED"], example = "COLLECTED")
    val resultType: String,
    @field:Schema(description = "FixtureCore UID", example = "abcd1234")
    val fixtureUid: String,
    @field:Schema(description = "실행 후 MatchCollect 상태. COLLECTED 가 아닌 경우 null", example = "SUCCESS", nullable = true)
    val status: MatchCollectStatus?,
    @field:Schema(description = "수집 시각. COLLECTED 가 아닌 경우 null", example = "2026-06-26T03:00:00Z", nullable = true)
    val collectedAt: Instant?,
    @field:Schema(description = "SKIPPED 사유", example = "League matchCollect is NONE", nullable = true)
    val reason: String?,
    @field:Schema(description = "FAILED 메시지", example = "provider failed", nullable = true)
    val message: String?,
    @field:Schema(description = "provider sync 결과. COLLECTED 가 아닌 경우 null", nullable = true)
    val syncResult: AdminMatchDataSyncResultResponse?,
)

@Schema(description = "운영자 단건 MatchCollect 실행 중 provider sync 결과")
data class AdminMatchDataSyncResultResponse(
    @field:Schema(description = "provider sync 결과 타입", allowableValues = ["PRE_MATCH", "LIVE", "POST_MATCH", "NOT_PLAYED", "ERROR"], example = "POST_MATCH")
    val resultType: String,
    @field:Schema(description = "provider 응답 기준 kickoff time", example = "2026-06-25T12:00:00Z", nullable = true)
    val kickoffTime: Instant?,
    @field:Schema(description = "라인업 캐시 여부. PRE_MATCH 결과에서 사용", example = "true", nullable = true)
    val lineupCached: Boolean? = null,
    @field:Schema(description = "pre-match job 종료 가능 여부. PRE_MATCH 결과에서 사용", example = "false", nullable = true)
    val shouldTerminatePreMatchJob: Boolean? = null,
    @field:Schema(description = "경기 진행 분. LIVE 결과에서 사용", example = "73", nullable = true)
    val elapsedMin: Int? = null,
    @field:Schema(description = "Fixture status code", example = "FT", nullable = true)
    val statusCode: FixtureStatusCode? = null,
    @field:Schema(description = "post-match polling 종료 가능 여부. POST_MATCH 결과에서 사용", example = "true", nullable = true)
    val shouldStopPolling: Boolean? = null,
    @field:Schema(description = "종료 후 경과 분. POST_MATCH 결과에서 사용", example = "120", nullable = true)
    val minutesSinceFinish: Long? = null,
    @field:Schema(description = "ERROR 결과 메시지", example = "provider failed", nullable = true)
    val message: String? = null,
)
