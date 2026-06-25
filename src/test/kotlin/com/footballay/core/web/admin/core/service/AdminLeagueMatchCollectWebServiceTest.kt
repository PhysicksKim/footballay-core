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
}
