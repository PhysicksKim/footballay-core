package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.persistence.core.entity.FixtureStatusCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.quartz.JobKey
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AvailableFixtureJobTransitionManagerTest {
    @Mock
    private lateinit var jobSchedulerService: JobSchedulerService

    private val fixtureUid = "testfixture0001"

    @Test
    fun `PreMatch shouldTerminatePreMatchJob=true이면 pre job을 삭제한다`() {
        val jobKey = JobKey.jobKey("pre-match-$fixtureUid", "pre-match")
        val manager = AvailableFixtureJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.PreMatch(true, Instant.now(), true),
            context = AvailableFixtureJobContext(AvailableFixtureJobPhase.PRE_MATCH, jobKey),
        )

        verify(jobSchedulerService).removeJob(jobKey)
        verify(jobSchedulerService, never()).addLiveMatchJob(any(), any())
    }

    @Test
    fun `PreMatch shouldTerminatePreMatchJob=false이면 job을 유지한다`() {
        val manager = AvailableFixtureJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.PreMatch(false, Instant.now(), false),
            context = AvailableFixtureJobContext(AvailableFixtureJobPhase.PRE_MATCH, JobKey.jobKey("pre", "pre-match")),
        )

        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).addLiveMatchJob(any(), any())
    }

    @Test
    fun `Live isMatchFinished=true이면 live job 삭제 후 post job을 등록한다`() {
        val jobKey = JobKey.jobKey("live-match-$fixtureUid", "live-match")
        val manager = AvailableFixtureJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.Live(Instant.now(), true, 90, "FT"),
            context = AvailableFixtureJobContext(AvailableFixtureJobPhase.LIVE_MATCH, jobKey),
        )

        verify(jobSchedulerService).removeJob(jobKey)
        verify(jobSchedulerService).addPostMatchJob(eq(fixtureUid), any())
    }

    @Test
    fun `Live isMatchFinished=false이면 job을 유지한다`() {
        val manager = AvailableFixtureJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.Live(Instant.now(), false, 45, "HT"),
            context = AvailableFixtureJobContext(AvailableFixtureJobPhase.LIVE_MATCH, JobKey.jobKey("live", "live-match")),
        )

        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).addPostMatchJob(any(), any())
    }

    @Test
    fun `PostMatch shouldStopPolling=true이면 post job을 삭제한다`() {
        val jobKey = JobKey.jobKey("post-match-$fixtureUid", "post-match")
        val manager = AvailableFixtureJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.PostMatch(Instant.now(), true, 65),
            context = AvailableFixtureJobContext(AvailableFixtureJobPhase.POST_MATCH, jobKey),
        )

        verify(jobSchedulerService).removeJob(jobKey)
    }

    @Test
    fun `PostMatch shouldStopPolling=false이면 job을 유지한다`() {
        val manager = AvailableFixtureJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.PostMatch(Instant.now(), false, 30),
            context = AvailableFixtureJobContext(AvailableFixtureJobPhase.POST_MATCH, JobKey.jobKey("post", "post-match")),
        )

        verify(jobSchedulerService, never()).removeJob(any())
    }

    @Test
    fun `NotPlayed이면 모든 available polling job을 정리하고 post job은 등록하지 않는다`() {
        val manager = AvailableFixtureJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.NotPlayed(FixtureStatusCode.CANC, Instant.now()),
            context = AvailableFixtureJobContext(AvailableFixtureJobPhase.LIVE_MATCH, JobKey.jobKey("live", "live-match")),
        )

        verify(jobSchedulerService).removeAllJobsForFixture(fixtureUid)
        verify(jobSchedulerService, never()).addPostMatchJob(any(), any())
    }

    @Test
    fun `Error이면 job을 유지한다`() {
        val manager = AvailableFixtureJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.Error("API error", null),
            context = AvailableFixtureJobContext(AvailableFixtureJobPhase.PRE_MATCH, JobKey.jobKey("pre", "pre-match")),
        )

        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).removeAllJobsForFixture(any())
        verify(jobSchedulerService, never()).addPostMatchJob(any(), any())
    }

    @Test
    fun `다른 phase에서 온 result는 무시한다`() {
        val manager = AvailableFixtureJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.PreMatch(true, Instant.now(), true),
            context = AvailableFixtureJobContext(AvailableFixtureJobPhase.LIVE_MATCH, JobKey.jobKey("live", "live-match")),
        )

        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).addPostMatchJob(any(), any())
    }
}
