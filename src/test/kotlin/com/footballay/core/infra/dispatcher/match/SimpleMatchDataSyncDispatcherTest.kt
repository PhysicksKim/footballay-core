package com.footballay.core.infra.dispatcher.match

import com.footballay.core.infra.match.MatchSyncOrchestrator
import com.footballay.core.infra.scheduler.JobSchedulerService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.quartz.JobKey
import java.time.Instant

/**
 * SimpleMatchDataSyncDispatcher 단위 테스트
 *
 * Job 전환 로직만 테스트합니다 (Match sync 내부 로직은 제외)
 * - MatchSyncOrchestrator와 JobSchedulerService를 Mock으로 주입
 * - verify()로 메서드 호출만 검증
 */
@ExtendWith(MockitoExtension::class)
class SimpleMatchDataSyncDispatcherTest {
    @Mock
    private lateinit var orchestrator: MatchSyncOrchestrator

    @Mock
    private lateinit var jobSchedulerService: JobSchedulerService

    private lateinit var dispatcher: SimpleMatchDataSyncDispatcher

    @BeforeEach
    fun setup() {
        dispatcher =
            SimpleMatchDataSyncDispatcher(
                orchestrators = listOf(orchestrator),
                jobSchedulerService = jobSchedulerService,
            )
    }

    @Test
    fun `PreMatch shouldTerminatePreMatchJob=true 시 PreMatchJob만 삭제 (LiveMatchJob 추가 안함)`() {
        // Given
        val fixtureUid = "testfixture0005"
        val jobKey = JobKey.jobKey("pre-match-$fixtureUid", "pre-match")
        val jobContext = JobContext(JobContext.JobPhase.PRE_MATCH, jobKey)

        val result =
            MatchDataSyncResult.PreMatch(
                lineupCached = true,
                kickoffTime = Instant.now(),
                shouldTerminatePreMatchJob = true,
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)
        whenever(jobSchedulerService.removeJob(jobKey)).thenReturn(true)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        verify(jobSchedulerService).removeJob(jobKey)
        verify(jobSchedulerService, never()).addLiveMatchJob(any(), any())
    }

    @Test
    fun `PreMatch shouldTerminatePreMatchJob=false 시 Job 유지`() {
        // Given
        val fixtureUid = "testfixture0006"
        val jobKey = JobKey.jobKey("pre-match-$fixtureUid", "pre-match")
        val jobContext = JobContext(JobContext.JobPhase.PRE_MATCH, jobKey)

        val result =
            MatchDataSyncResult.PreMatch(
                lineupCached = false,
                kickoffTime = Instant.now(),
                shouldTerminatePreMatchJob = false,
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).addLiveMatchJob(any(), any())
    }

    @Test
    fun `Live isMatchFinished=true 시 PostMatchJob으로 전환`() {
        // Given
        val fixtureUid = "testfixture0007"
        val jobKey = JobKey.jobKey("live-match-$fixtureUid", "live-match")
        val jobContext = JobContext(JobContext.JobPhase.LIVE_MATCH, jobKey)

        val result =
            MatchDataSyncResult.Live(
                kickoffTime = Instant.now(),
                isMatchFinished = true,
                elapsedMin = 90,
                statusShort = "FT",
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)
        whenever(jobSchedulerService.removeJob(jobKey)).thenReturn(true)
        whenever(jobSchedulerService.addPostMatchJob(any(), any())).thenReturn(true)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        verify(jobSchedulerService).removeJob(jobKey)
        verify(jobSchedulerService).addPostMatchJob(eq(fixtureUid), any())
    }

    @Test
    fun `Live isMatchFinished=false 시 Job 유지`() {
        // Given
        val fixtureUid = "testfixture0008"
        val jobKey = JobKey.jobKey("live-match-$fixtureUid", "live-match")
        val jobContext = JobContext(JobContext.JobPhase.LIVE_MATCH, jobKey)

        val result =
            MatchDataSyncResult.Live(
                kickoffTime = Instant.now(),
                isMatchFinished = false,
                elapsedMin = 45,
                statusShort = "HT",
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).addPostMatchJob(any(), any())
    }

    @Test
    fun `PostMatch shouldStopPolling=true 시 Job 삭제`() {
        // Given
        val fixtureUid = "testfixture0009"
        val jobKey = JobKey.jobKey("post-match-$fixtureUid", "post-match")
        val jobContext = JobContext(JobContext.JobPhase.POST_MATCH, jobKey)

        val result =
            MatchDataSyncResult.PostMatch(
                kickoffTime = Instant.now(),
                shouldStopPolling = true,
                minutesSinceFinish = 65,
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)
        whenever(jobSchedulerService.removeJob(jobKey)).thenReturn(true)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        verify(jobSchedulerService).removeJob(jobKey)
    }

    @Test
    fun `PostMatch shouldStopPolling=false 시 Job 유지`() {
        // Given
        val fixtureUid = "testfixture0010"
        val jobKey = JobKey.jobKey("post-match-$fixtureUid", "post-match")
        val jobContext = JobContext(JobContext.JobPhase.POST_MATCH, jobKey)

        val result =
            MatchDataSyncResult.PostMatch(
                kickoffTime = Instant.now(),
                shouldStopPolling = false,
                minutesSinceFinish = 30,
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        verify(jobSchedulerService, never()).removeJob(any())
    }

    @Test
    fun `Error 발생 시 Job은 계속 실행 (전환 없음)`() {
        // Given
        val fixtureUid = "testfixture0011"
        val jobKey = JobKey.jobKey("pre-match-$fixtureUid", "pre-match")
        val jobContext = JobContext(JobContext.JobPhase.PRE_MATCH, jobKey)

        val result =
            MatchDataSyncResult.Error(
                message = "API error",
                kickoffTime = null,
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).addLiveMatchJob(any(), any())
        verify(jobSchedulerService, never()).addPostMatchJob(any(), any())
    }

    @Test
    fun `LiveMatchJob이 PreMatch Result를 받으면 무시하고 계속 실행`() {
        // Given
        val fixtureUid = "testfixture0012"
        val jobKey = JobKey.jobKey("live-match-$fixtureUid", "live-match")
        val jobContext = JobContext(JobContext.JobPhase.LIVE_MATCH, jobKey) // LiveMatchJob

        val result =
            MatchDataSyncResult.PreMatch( // PreMatch Result (잘못된 조합)
                lineupCached = true,
                kickoffTime = Instant.now(),
                shouldTerminatePreMatchJob = true,
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        // Job 삭제가 일어나지 않아야 함 (무시됨)
        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).addLiveMatchJob(any(), any())
    }

    @Test
    fun `PreMatchJob이 Live Result를 받으면 무시하고 계속 실행`() {
        // Given
        val fixtureUid = "testfixture0013"
        val jobKey = JobKey.jobKey("pre-match-$fixtureUid", "pre-match")
        val jobContext = JobContext(JobContext.JobPhase.PRE_MATCH, jobKey) // PreMatchJob

        val result =
            MatchDataSyncResult.Live( // Live Result (잘못된 조합)
                kickoffTime = Instant.now(),
                isMatchFinished = false,
                elapsedMin = 10,
                statusShort = "1H",
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        // Job 삭제/전환이 일어나지 않아야 함 (무시됨)
        verify(jobSchedulerService, never()).removeJob(any())
        verify(jobSchedulerService, never()).addPostMatchJob(any(), any())
    }

    @Test
    fun `LiveMatchJob이 PostMatch Result를 받으면 무시하고 계속 실행`() {
        // Given
        val fixtureUid = "testfixture0014"
        val jobKey = JobKey.jobKey("live-match-$fixtureUid", "live-match")
        val jobContext = JobContext(JobContext.JobPhase.LIVE_MATCH, jobKey) // LiveMatchJob

        val result =
            MatchDataSyncResult.PostMatch( // PostMatch Result (잘못된 조합)
                kickoffTime = Instant.now(),
                shouldStopPolling = true,
                minutesSinceFinish = 65,
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(result)

        // When
        val syncResult = dispatcher.syncByFixtureUid(fixtureUid, jobContext)

        // Then
        assertThat(syncResult).isEqualTo(result)
        // Job 삭제가 일어나지 않아야 함 (무시됨)
        verify(jobSchedulerService, never()).removeJob(any())
    }
}
