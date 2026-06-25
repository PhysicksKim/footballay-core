package com.footballay.core.web.admin.core.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.facade.LeagueMatchCollectFacade
import com.footballay.core.infra.matchcollect.MatchCollectExecutionResult
import com.footballay.core.web.admin.core.dto.AdminMatchCollectExecutionResponse
import com.footballay.core.web.admin.core.dto.AdminMatchDataSyncResultResponse
import com.footballay.core.web.admin.core.dto.MatchCollectUpdateResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class AdminLeagueMatchCollectWebService(
    private val leagueMatchCollectFacade: LeagueMatchCollectFacade,
    private val clock: Clock = Clock.systemUTC(),
) {
    @PreAuthorize("hasRole('ADMIN')")
    fun setLeagueMatchCollect(
        leagueCoreUid: String,
        matchCollect: MatchCollect,
    ): DomainResult<MatchCollectUpdateResponse, DomainFail> =
        leagueMatchCollectFacade
            .setLeagueMatchCollectByCoreUid(leagueCoreUid, matchCollect)
            .map {
                MatchCollectUpdateResponse(
                    uid = it.leagueCoreUid,
                    matchCollect = it.matchCollect,
                    reconcileSuccess = it.reconcileResult.success,
                )
            }

    @PreAuthorize("hasRole('ADMIN')")
    fun collectMatchByFixtureUid(fixtureUid: String): DomainResult<AdminMatchCollectExecutionResponse, DomainFail> =
        leagueMatchCollectFacade
            .collectMatchByFixtureUidIgnoringSchedule(fixtureUid, Instant.now(clock))
            .map(::toExecutionResponse)

    private fun toExecutionResponse(result: MatchCollectExecutionResult): AdminMatchCollectExecutionResponse =
        when (result) {
            is MatchCollectExecutionResult.Collected ->
                AdminMatchCollectExecutionResponse(
                    resultType = "COLLECTED",
                    fixtureUid = result.fixtureUid,
                    status = result.status,
                    collectedAt = result.collectedAt,
                    reason = null,
                    message = null,
                    syncResult = toSyncResultResponse(result.syncResult),
                )

            is MatchCollectExecutionResult.Skipped ->
                AdminMatchCollectExecutionResponse(
                    resultType = "SKIPPED",
                    fixtureUid = result.fixtureUid,
                    status = null,
                    collectedAt = null,
                    reason = result.reason,
                    message = null,
                    syncResult = null,
                )

            is MatchCollectExecutionResult.Failed ->
                AdminMatchCollectExecutionResponse(
                    resultType = "FAILED",
                    fixtureUid = result.fixtureUid,
                    status = null,
                    collectedAt = null,
                    reason = null,
                    message = result.message,
                    syncResult = null,
                )
        }

    private fun toSyncResultResponse(result: MatchDataSyncResult): AdminMatchDataSyncResultResponse =
        when (result) {
            is MatchDataSyncResult.PreMatch ->
                AdminMatchDataSyncResultResponse(
                    resultType = "PRE_MATCH",
                    kickoffTime = result.kickoffTime,
                    lineupCached = result.lineupCached,
                    shouldTerminatePreMatchJob = result.shouldTerminatePreMatchJob,
                )

            is MatchDataSyncResult.Live ->
                AdminMatchDataSyncResultResponse(
                    resultType = "LIVE",
                    kickoffTime = result.kickoffTime,
                    elapsedMin = result.elapsedMin,
                    statusCode = result.statusCode,
                )

            is MatchDataSyncResult.PostMatch ->
                AdminMatchDataSyncResultResponse(
                    resultType = "POST_MATCH",
                    kickoffTime = result.kickoffTime,
                    shouldStopPolling = result.shouldStopPolling,
                    minutesSinceFinish = result.minutesSinceFinish,
                )

            is MatchDataSyncResult.NotPlayed ->
                AdminMatchDataSyncResultResponse(
                    resultType = "NOT_PLAYED",
                    kickoffTime = result.kickoffTime,
                    statusCode = result.statusCode,
                )

            is MatchDataSyncResult.Error ->
                AdminMatchDataSyncResultResponse(
                    resultType = "ERROR",
                    kickoffTime = result.kickoffTime,
                    message = result.message,
                )
        }
}
