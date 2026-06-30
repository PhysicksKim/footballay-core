package com.footballay.core.infra.scheduler

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.scheduler.matchjob.MatchJobIdentity
import com.footballay.core.infra.scheduler.matchjob.MatchJobKeyFactory
import com.footballay.core.infra.scheduler.matchjob.MatchJobOwner
import com.footballay.core.infra.scheduler.matchjob.MatchJobPhase
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
class MatchCollectLiveJobTransitionManagerTest {
    @Mock
    private lateinit var jobSchedulerService: JobSchedulerService

    private val fixtureUid = "fixture-1"

    @Test
    fun `pre result가 terminate true이면 pre job을 삭제한다`() {
        val jobKey = matchCollectJobKey(MatchJobPhase.PRE)
        val manager = MatchCollectLiveJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.PreMatch(true, Instant.now(), true),
            context = MatchCollectLiveJobContext(MatchCollectLiveJobPhase.PRE, jobKey),
        )

        verify(jobSchedulerService).removeJob(jobKey)
    }

    @Test
    fun `live job에서 PostMatch result를 받으면 live job 삭제 후 post job을 등록한다`() {
        val jobKey = matchCollectJobKey(MatchJobPhase.LIVE)
        val manager = MatchCollectLiveJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.PostMatch(Instant.now(), false, 0),
            context = MatchCollectLiveJobContext(MatchCollectLiveJobPhase.LIVE, jobKey),
        )

        verify(jobSchedulerService).removeJob(jobKey)
        verify(jobSchedulerService).registerOrReplaceMatchCollectJob(
            phase = eq(MatchJobPhase.POST),
            leagueUid = eq("league-1"),
            fixtureUid = eq(fixtureUid),
            startTime = any(),
            compareStartAt = eq(false),
        )
    }

    @Test
    fun `post job에서 shouldStopPolling true이면 post job을 삭제한다`() {
        val jobKey = matchCollectJobKey(MatchJobPhase.POST)
        val manager = MatchCollectLiveJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.PostMatch(Instant.now(), true, 90),
            context = MatchCollectLiveJobContext(MatchCollectLiveJobPhase.POST, jobKey),
        )

        verify(jobSchedulerService).removeJob(jobKey)
    }

    @Test
    fun `not played이면 matchcollect fixture jobs를 삭제한다`() {
        val manager = MatchCollectLiveJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.NotPlayed(FixtureStatusCode.CANC, Instant.now()),
            context = MatchCollectLiveJobContext(MatchCollectLiveJobPhase.LIVE, matchCollectJobKey(MatchJobPhase.LIVE)),
        )

        verify(jobSchedulerService).deleteFixtureJobs("league-1", fixtureUid, MatchJobOwner.MATCHCOLLECT)
        verify(jobSchedulerService, never()).registerOrReplaceMatchCollectJob(any(), any(), any(), any(), any())
    }

    @Test
    fun `error이면 job을 유지한다`() {
        val manager = MatchCollectLiveJobTransitionManager(jobSchedulerService)

        manager.handle(
            fixtureUid = fixtureUid,
            result = MatchDataSyncResult.Error("provider failed", null),
            context = MatchCollectLiveJobContext(MatchCollectLiveJobPhase.LIVE, matchCollectJobKey(MatchJobPhase.LIVE)),
        )

        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).deleteFixtureJobs(any(), any(), any())
        verify(jobSchedulerService, never()).registerOrReplaceMatchCollectJob(any(), any(), any(), any(), any())
    }

    private fun matchCollectJobKey(phase: MatchJobPhase): JobKey =
        MatchJobKeyFactory.jobKey(
            MatchJobIdentity(
                owner = MatchJobOwner.MATCHCOLLECT,
                phase = phase,
                leagueUid = "league-1",
                fixtureUid = fixtureUid,
            ),
        )
}
