package com.footballay.core.infra.scheduler.cleanup

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.impl.matchers.GroupMatcher

@ExtendWith(MockitoExtension::class)
class StartupMatchJobCleanupServiceTest {
    @Mock
    private lateinit var scheduler: Scheduler

    @Test
    fun `startup rebuild cleanup은 legacy available group과 current available owner job만 삭제한다`() {
        val service = StartupMatchJobCleanupService(scheduler)
        val oldPreJob = JobKey.jobKey("pre-match-fixture-1", "pre-match")
        val oldLiveJob = JobKey.jobKey("live-match-fixture-1", "live-match")
        val currentAvailableJob = JobKey.jobKey("available:pre:fixture-2", "league:match:league-1")
        val currentMatchCollectJob = JobKey.jobKey("matchcollect:live:fixture-3", "league:match:league-1")
        val legacyFootballJob = JobKey.jobKey("live-match-100", "football-fixture")

        whenever(scheduler.getJobKeys(any<GroupMatcher<JobKey>>()))
            .thenReturn(setOf(oldPreJob))
            .thenReturn(setOf(oldLiveJob))
            .thenReturn(emptySet())
            .thenReturn(setOf(currentAvailableJob, currentMatchCollectJob))
        whenever(scheduler.jobGroupNames).thenReturn(listOf("league:match:league-1", "football-fixture"))
        whenever(scheduler.deleteJob(any())).thenReturn(true)

        val result = service.deleteAvailableMatchJobsForStartupRebuild()

        assertThat(result.success).isTrue()
        assertThat(result.deleted).isEqualTo(3)
        assertThat(result.skipped).isEqualTo(1)
        verify(scheduler).deleteJob(oldPreJob)
        verify(scheduler).deleteJob(oldLiveJob)
        verify(scheduler).deleteJob(currentAvailableJob)
        verify(scheduler, never()).deleteJob(currentMatchCollectJob)
        verify(scheduler, never()).deleteJob(legacyFootballJob)
    }

    @Test
    fun `startup rebuild cleanup은 group 조회 실패를 error로 기록한다`() {
        val service = StartupMatchJobCleanupService(scheduler)
        whenever(scheduler.getJobKeys(any<GroupMatcher<JobKey>>())).thenThrow(RuntimeException("quartz failed"))
        whenever(scheduler.jobGroupNames).thenReturn(emptyList())

        val result = service.deleteAvailableMatchJobsForStartupRebuild()

        assertThat(result.success).isFalse()
        assertThat(result.errors).isNotEmpty
        assertThat(result.errors.first().operation).isEqualTo("list-jobs:legacy-available-group")
    }
}
