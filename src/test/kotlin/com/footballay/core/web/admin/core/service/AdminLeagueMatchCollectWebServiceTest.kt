package com.footballay.core.web.admin.core.service

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.facade.LeagueMatchCollectFacade
import com.footballay.core.infra.facade.LeagueMatchCollectUpdateResult
import com.footballay.core.infra.matchcollect.MatchCollectExecutionResult
import com.footballay.core.infra.scheduler.ReconcileResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class AdminLeagueMatchCollectWebServiceTest {
    @Mock
    private lateinit var leagueMatchCollectFacade: LeagueMatchCollectFacade

    private val now = Instant.parse("2026-06-25T00:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `matchCollect 변경 결과를 admin response로 변환한다`() {
        val leagueUid = "league-1"
        val webService = AdminLeagueMatchCollectWebService(leagueMatchCollectFacade, clock)
        given(leagueMatchCollectFacade.setLeagueMatchCollectByCoreUid(leagueUid, MatchCollect.FINISHED))
            .willReturn(
                DomainResult.Success(
                    LeagueMatchCollectUpdateResult(
                        leagueCoreUid = leagueUid,
                        matchCollect = MatchCollect.FINISHED,
                        reconcileResult = ReconcileResult.empty(fixtureUid = null, leagueUid = leagueUid),
                    ),
                ),
            )

        val result = webService.setLeagueMatchCollect(leagueUid, MatchCollect.FINISHED)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val response = (result as DomainResult.Success).value
        assertThat(response.uid).isEqualTo(leagueUid)
        assertThat(response.matchCollect).isEqualTo(MatchCollect.FINISHED)
        assertThat(response.reconcileSuccess).isTrue()
    }

    @Test
    fun `admin 단건 match collect 결과를 response로 변환한다`() {
        val fixtureUid = "fixture-1"
        val kickoff = Instant.parse("2026-06-24T12:00:00Z")
        val webService = AdminLeagueMatchCollectWebService(leagueMatchCollectFacade, clock)

        given(leagueMatchCollectFacade.collectMatchByFixtureUidIgnoringSchedule(fixtureUid, now))
            .willReturn(
                DomainResult.Success(
                    MatchCollectExecutionResult.Collected(
                        fixtureUid = fixtureUid,
                        status = MatchCollectStatus.SUCCESS,
                        collectedAt = now,
                        syncResult =
                            MatchDataSyncResult.PostMatch(
                                kickoffTime = kickoff,
                                shouldStopPolling = true,
                                minutesSinceFinish = 120,
                            ),
                    ),
                ),
            )

        val result = webService.collectMatchByFixtureUid(fixtureUid)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val response = (result as DomainResult.Success).value
        assertThat(response.resultType).isEqualTo("COLLECTED")
        assertThat(response.fixtureUid).isEqualTo(fixtureUid)
        assertThat(response.status).isEqualTo(MatchCollectStatus.SUCCESS)
        assertThat(response.collectedAt).isEqualTo(now)
        assertThat(response.syncResult?.resultType).isEqualTo("POST_MATCH")
        assertThat(response.syncResult?.statusCode).isNull()
    }

    @Test
    fun `admin 단건 match collect skipped 결과를 response로 변환한다`() {
        val fixtureUid = "fixture-skip"
        val webService = AdminLeagueMatchCollectWebService(leagueMatchCollectFacade, clock)

        given(leagueMatchCollectFacade.collectMatchByFixtureUidIgnoringSchedule(fixtureUid, now))
            .willReturn(DomainResult.Success(MatchCollectExecutionResult.Skipped(fixtureUid, "League matchCollect is NONE")))

        val result = webService.collectMatchByFixtureUid(fixtureUid)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val response = (result as DomainResult.Success).value
        assertThat(response.resultType).isEqualTo("SKIPPED")
        assertThat(response.reason).isEqualTo("League matchCollect is NONE")
    }

    @Test
    fun `admin 단건 match collect failed 결과를 response로 변환한다`() {
        val fixtureUid = "fixture-failed"
        val webService = AdminLeagueMatchCollectWebService(leagueMatchCollectFacade, clock)

        given(leagueMatchCollectFacade.collectMatchByFixtureUidIgnoringSchedule(fixtureUid, now))
            .willReturn(DomainResult.Success(MatchCollectExecutionResult.Failed(fixtureUid, "provider failed")))

        val result = webService.collectMatchByFixtureUid(fixtureUid)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val response = (result as DomainResult.Success).value
        assertThat(response.resultType).isEqualTo("FAILED")
        assertThat(response.fixtureUid).isEqualTo(fixtureUid)
        assertThat(response.status).isNull()
        assertThat(response.collectedAt).isNull()
        assertThat(response.reason).isNull()
        assertThat(response.message).isEqualTo("provider failed")
        assertThat(response.syncResult).isNull()
    }

    @Test
    fun `admin 단건 match collect pre match syncResult를 response로 변환한다`() {
        val fixtureUid = "fixture-pre"
        val kickoff = Instant.parse("2026-06-25T12:00:00Z")
        val webService = AdminLeagueMatchCollectWebService(leagueMatchCollectFacade, clock)

        given(leagueMatchCollectFacade.collectMatchByFixtureUidIgnoringSchedule(fixtureUid, now))
            .willReturn(
                collected(
                    fixtureUid = fixtureUid,
                    syncResult =
                        MatchDataSyncResult.PreMatch(
                            lineupCached = true,
                            kickoffTime = kickoff,
                            shouldTerminatePreMatchJob = false,
                        ),
                ),
            )

        val response = (webService.collectMatchByFixtureUid(fixtureUid) as DomainResult.Success).value

        assertThat(response.syncResult?.resultType).isEqualTo("PRE_MATCH")
        assertThat(response.syncResult?.kickoffTime).isEqualTo(kickoff)
        assertThat(response.syncResult?.lineupCached).isTrue()
        assertThat(response.syncResult?.shouldTerminatePreMatchJob).isFalse()
    }

    @Test
    fun `admin 단건 match collect live syncResult를 response로 변환한다`() {
        val fixtureUid = "fixture-live"
        val kickoff = Instant.parse("2026-06-25T12:00:00Z")
        val webService = AdminLeagueMatchCollectWebService(leagueMatchCollectFacade, clock)

        given(leagueMatchCollectFacade.collectMatchByFixtureUidIgnoringSchedule(fixtureUid, now))
            .willReturn(
                collected(
                    fixtureUid = fixtureUid,
                    syncResult =
                        MatchDataSyncResult.Live(
                            kickoffTime = kickoff,
                            elapsedMin = 73,
                            statusCode = com.footballay.core.domain.fixture.FixtureStatusCode.SECOND_HALF,
                        ),
                ),
            )

        val response = (webService.collectMatchByFixtureUid(fixtureUid) as DomainResult.Success).value

        assertThat(response.syncResult?.resultType).isEqualTo("LIVE")
        assertThat(response.syncResult?.kickoffTime).isEqualTo(kickoff)
        assertThat(response.syncResult?.elapsedMin).isEqualTo(73)
        assertThat(response.syncResult?.statusCode).isEqualTo(com.footballay.core.domain.fixture.FixtureStatusCode.SECOND_HALF)
    }

    @Test
    fun `admin 단건 match collect not played syncResult를 response로 변환한다`() {
        val fixtureUid = "fixture-not-played"
        val kickoff = Instant.parse("2026-06-25T12:00:00Z")
        val webService = AdminLeagueMatchCollectWebService(leagueMatchCollectFacade, clock)

        given(leagueMatchCollectFacade.collectMatchByFixtureUidIgnoringSchedule(fixtureUid, now))
            .willReturn(
                collected(
                    fixtureUid = fixtureUid,
                    status = MatchCollectStatus.NOT_PLAYED,
                    syncResult = MatchDataSyncResult.NotPlayed(com.footballay.core.domain.fixture.FixtureStatusCode.CANC, kickoff),
                ),
            )

        val response = (webService.collectMatchByFixtureUid(fixtureUid) as DomainResult.Success).value

        assertThat(response.syncResult?.resultType).isEqualTo("NOT_PLAYED")
        assertThat(response.syncResult?.kickoffTime).isEqualTo(kickoff)
        assertThat(response.syncResult?.statusCode).isEqualTo(com.footballay.core.domain.fixture.FixtureStatusCode.CANC)
    }

    @Test
    fun `admin 단건 match collect error syncResult를 response로 변환한다`() {
        val fixtureUid = "fixture-error"
        val kickoff = Instant.parse("2026-06-25T12:00:00Z")
        val webService = AdminLeagueMatchCollectWebService(leagueMatchCollectFacade, clock)

        given(leagueMatchCollectFacade.collectMatchByFixtureUidIgnoringSchedule(fixtureUid, now))
            .willReturn(
                collected(
                    fixtureUid = fixtureUid,
                    syncResult = MatchDataSyncResult.Error("provider error", kickoff),
                ),
            )

        val response = (webService.collectMatchByFixtureUid(fixtureUid) as DomainResult.Success).value

        assertThat(response.syncResult?.resultType).isEqualTo("ERROR")
        assertThat(response.syncResult?.kickoffTime).isEqualTo(kickoff)
        assertThat(response.syncResult?.message).isEqualTo("provider error")
    }

    private fun collected(
        fixtureUid: String,
        status: MatchCollectStatus = MatchCollectStatus.EARLY_SYNCED,
        syncResult: MatchDataSyncResult,
    ): DomainResult.Success<MatchCollectExecutionResult.Collected> =
        DomainResult.Success(
            MatchCollectExecutionResult.Collected(
                fixtureUid = fixtureUid,
                status = status,
                collectedAt = now,
                syncResult = syncResult,
            ),
        )
}
