package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.MatchDataSyncDispatcher
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AvailableFixtureJobTest {
    @Mock
    private lateinit var dispatcher: MatchDataSyncDispatcher

    @Mock
    private lateinit var transitionManager: AvailableFixtureJobTransitionManager

    @Mock
    private lateinit var cacheRefreshPublisher: AvailableFixtureCacheRefreshPublisher

    @Mock
    private lateinit var executionContext: JobExecutionContext

    @Mock
    private lateinit var jobDetail: JobDetail

    @Test
    fun `PreMatchJob은 sync 후 transition과 cache refresh를 호출한다`() {
        val fixtureUid = "fixture-pre"
        val jobKey = JobKey.jobKey("pre-match-$fixtureUid", "pre-match")
        val result = MatchDataSyncResult.PreMatch(true, Instant.now(), true)
        whenever(dispatcher.syncByFixtureUid(fixtureUid)).thenReturn(result)
        setupExecutionContext(PreMatchJob.KEY_FIXTURE_UID, fixtureUid, jobKey)

        PreMatchJob(dispatcher, transitionManager, cacheRefreshPublisher).execute(executionContext)

        assertJobFlow(fixtureUid, result, AvailableFixtureJobPhase.PRE_MATCH, jobKey)
    }

    @Test
    fun `LiveMatchJob은 sync 후 transition과 cache refresh를 호출한다`() {
        val fixtureUid = "fixture-live"
        val jobKey = JobKey.jobKey("live-match-$fixtureUid", "live-match")
        val result = MatchDataSyncResult.Live(Instant.now(), false, 20, "1H")
        whenever(dispatcher.syncByFixtureUid(fixtureUid)).thenReturn(result)
        setupExecutionContext(LiveMatchJob.KEY_FIXTURE_UID, fixtureUid, jobKey)

        LiveMatchJob(dispatcher, transitionManager, cacheRefreshPublisher).execute(executionContext)

        assertJobFlow(fixtureUid, result, AvailableFixtureJobPhase.LIVE_MATCH, jobKey)
    }

    @Test
    fun `PostMatchJob은 sync 후 transition과 cache refresh를 호출한다`() {
        val fixtureUid = "fixture-post"
        val jobKey = JobKey.jobKey("post-match-$fixtureUid", "post-match")
        val result = MatchDataSyncResult.PostMatch(Instant.now(), false, 30)
        whenever(dispatcher.syncByFixtureUid(fixtureUid)).thenReturn(result)
        setupExecutionContext(PostMatchJob.KEY_FIXTURE_UID, fixtureUid, jobKey)

        PostMatchJob(dispatcher, transitionManager, cacheRefreshPublisher).execute(executionContext)

        assertJobFlow(fixtureUid, result, AvailableFixtureJobPhase.POST_MATCH, jobKey)
    }

    private fun setupExecutionContext(
        fixtureUidKey: String,
        fixtureUid: String,
        jobKey: JobKey,
    ) {
        whenever(executionContext.mergedJobDataMap).thenReturn(JobDataMap(mapOf(fixtureUidKey to fixtureUid)))
        whenever(executionContext.jobDetail).thenReturn(jobDetail)
        whenever(jobDetail.key).thenReturn(jobKey)
    }

    private fun assertJobFlow(
        fixtureUid: String,
        result: MatchDataSyncResult,
        phase: AvailableFixtureJobPhase,
        jobKey: JobKey,
    ) {
        val contextCaptor = argumentCaptor<AvailableFixtureJobContext>()

        verify(dispatcher).syncByFixtureUid(fixtureUid)
        verify(transitionManager).handle(eq(fixtureUid), eq(result), contextCaptor.capture())
        verify(cacheRefreshPublisher).publishIfNeeded(fixtureUid, result, phase)
        assertThat(contextCaptor.firstValue.phase).isEqualTo(phase)
        assertThat(contextCaptor.firstValue.jobKey).isEqualTo(jobKey)
    }
}
