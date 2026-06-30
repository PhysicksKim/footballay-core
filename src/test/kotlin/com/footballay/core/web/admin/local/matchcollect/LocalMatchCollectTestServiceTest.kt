package com.footballay.core.web.admin.local.matchcollect

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.matchcollect.FinishedMatchCollectBatchResult
import com.footballay.core.infra.matchcollect.LeagueMatchCollectManager
import com.footballay.core.infra.matchcollect.MatchCollectExecutionResult
import com.footballay.core.infra.matchcollect.MatchCollectSyncExecutor
import com.footballay.core.infra.scheduler.MatchCollectLiveJobReconciler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.quartz.JobKey
import org.quartz.Scheduler
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class LocalMatchCollectTestServiceTest {
    @Mock
    private lateinit var queryFacade: LocalMatchCollectTestQueryFacade

    @Mock
    private lateinit var liveJobReconciler: MatchCollectLiveJobReconciler

    @Mock
    private lateinit var leagueMatchCollectManager: LeagueMatchCollectManager

    @Mock
    private lateinit var matchCollectSyncExecutor: MatchCollectSyncExecutor

    @Mock
    private lateinit var scheduler: Scheduler

    private val now = Instant.parse("2026-06-26T03:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `diagnostics는 query facade 결과를 반환하고 quartz 제외 시 scheduler를 조회하지 않는다`() {
        val query =
            LocalMatchCollectDiagnosticsQuery(
                league =
                    LocalMatchCollectLeagueSnapshot(
                        leagueCoreUid = "league-1",
                        name = "League",
                        available = true,
                        matchCollect = MatchCollect.LIVE,
                    ),
                fixture =
                    LocalMatchCollectFixtureSnapshot(
                        fixtureUid = "fixture-1",
                        leagueCoreUid = "league-1",
                        seasonYear = 2026,
                        currentSeason = true,
                        kickoff = now,
                        statusCode = FixtureStatusCode.NS,
                        available = false,
                        apiSportsFixtureId = 123L,
                    ),
                state =
                    LocalMatchCollectStateSnapshot(
                        fixtureUid = "fixture-1",
                        matchCollectStatus = MatchCollectStatus.FAIL_END,
                        lastCollectedAt = now,
                    ),
                recentStates = emptyList(),
            )
        whenever(
            queryFacade.diagnostics(
                leagueUid = "league-1",
                fixtureUid = "fixture-1",
                includeState = true,
            ),
        ).thenReturn(query)

        val result =
            service().diagnostics(
                leagueUid = "league-1",
                fixtureUid = "fixture-1",
                includeQuartz = false,
                includeState = true,
            )

        assertThat(result.league?.leagueCoreUid).isEqualTo("league-1")
        assertThat(result.fixture?.fixtureUid).isEqualTo("fixture-1")
        assertThat(result.state?.matchCollectStatus).isEqualTo(MatchCollectStatus.FAIL_END)
        assertThat(result.quartzJobs).isEmpty()
        verifyNoInteractions(scheduler)
    }

    @Test
    fun `runFinishedScannerOnce는 batchSize를 1 이상 500 이하로 보정한다`() {
        whenever(leagueMatchCollectManager.collectDueFinishedFixtures(now, 500)).thenReturn(emptyBatch())
        whenever(leagueMatchCollectManager.collectDueFinishedFixtures(now, 1)).thenReturn(emptyBatch())

        service().runFinishedScannerOnce(now = now, batchSize = 999)
        service().runFinishedScannerOnce(now = now, batchSize = 0)

        verify(leagueMatchCollectManager).collectDueFinishedFixtures(now, 500)
        verify(leagueMatchCollectManager).collectDueFinishedFixtures(now, 1)
    }

    @Test
    fun `collect methods는 phase별 executor method로 위임한다`() {
        whenever(matchCollectSyncExecutor.collectFinished("fixture-1", now)).thenReturn(skipped("fixture-1"))
        whenever(matchCollectSyncExecutor.collectPre("fixture-1", now)).thenReturn(skipped("fixture-1"))
        whenever(matchCollectSyncExecutor.collectLive("fixture-1", now)).thenReturn(skipped("fixture-1"))
        whenever(matchCollectSyncExecutor.collectPost("fixture-1", now)).thenReturn(skipped("fixture-1"))

        service().collectFinished("fixture-1", now)
        service().collectPre("fixture-1", now)
        service().collectLive("fixture-1", now)
        service().collectPost("fixture-1", now)

        verify(matchCollectSyncExecutor).collectFinished("fixture-1", now)
        verify(matchCollectSyncExecutor).collectPre("fixture-1", now)
        verify(matchCollectSyncExecutor).collectLive("fixture-1", now)
        verify(matchCollectSyncExecutor).collectPost("fixture-1", now)
    }

    @Test
    fun `fireQuartzJob은 job이 없으면 trigger하지 않는다`() {
        val jobKey = JobKey.jobKey("job-1", "group-1")
        whenever(scheduler.checkExists(jobKey)).thenReturn(false)

        val result =
            service().fireQuartzJob(
                LocalQuartzFireRequest(
                    jobGroup = "group-1",
                    jobName = "job-1",
                ),
            )

        assertThat(result.triggered).isFalse()
        assertThat(result.message).isEqualTo("Quartz job not found")
        verify(scheduler, never()).triggerJob(eq(jobKey))
    }

    @Test
    fun `fireQuartzJob은 job이 있으면 scheduler triggerJob을 호출한다`() {
        val jobKey = JobKey.jobKey("job-1", "group-1")
        whenever(scheduler.checkExists(jobKey)).thenReturn(true)

        val result =
            service().fireQuartzJob(
                LocalQuartzFireRequest(
                    jobGroup = "group-1",
                    jobName = "job-1",
                ),
            )

        assertThat(result.triggered).isTrue()
        assertThat(result.message).isEqualTo("Quartz job trigger requested")
        verify(scheduler).triggerJob(jobKey)
    }

    private fun service() =
        LocalMatchCollectTestService(
            localMatchCollectTestQueryFacade = queryFacade,
            matchCollectLiveJobReconciler = liveJobReconciler,
            leagueMatchCollectManager = leagueMatchCollectManager,
            matchCollectSyncExecutor = matchCollectSyncExecutor,
            scheduler = scheduler,
            clock = clock,
        )

    private fun emptyBatch() =
        FinishedMatchCollectBatchResult(
            candidates = 0,
            due = 0,
            collected = 0,
            skipped = 0,
            failed = 0,
            results = emptyList(),
        )

    private fun skipped(fixtureUid: String) =
        MatchCollectExecutionResult.Skipped(
            fixtureUid = fixtureUid,
            reason = "skip",
        )
}
