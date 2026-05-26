package com.footballay.core.infra.scheduler.matchjob

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.Trigger
import java.time.Instant
import java.util.Date

@ExtendWith(MockitoExtension::class)
class LegacyAvailableMatchJobRegistrarTest {
    @Mock
    private lateinit var scheduler: Scheduler

    @Test
    fun `legacy pre job은 이전 group과 job name 규칙으로 등록한다`() {
        val registrar = registrar()
        val startAt = Instant.parse("2026-05-14T10:00:00Z")
        val jobCaptor = argumentCaptor<JobDetail>()
        val triggerCaptor = argumentCaptor<Trigger>()
        whenever(scheduler.checkExists(JobKey.jobKey("pre-match-fixture-1", "pre-match"))).thenReturn(false)
        whenever(scheduler.scheduleJob(any<JobDetail>(), any<Trigger>())).thenReturn(Date.from(startAt))

        @Suppress("DEPRECATION")
        val result = registrar.addPreMatchJob("fixture-1", startAt)

        assertThat(result).isTrue()
        verify(scheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture())
        assertThat(jobCaptor.firstValue.key).isEqualTo(JobKey.jobKey("pre-match-fixture-1", "pre-match"))
        assertThat(jobCaptor.firstValue.jobDataMap.getString(MatchJobDataKeys.KEY_FIXTURE_UID)).isEqualTo("fixture-1")
        assertThat(triggerCaptor.firstValue.key.name).isEqualTo("pre-match-trigger-fixture-1")
        assertThat(triggerCaptor.firstValue.key.group).isEqualTo("pre-match")
    }

    @Test
    fun `legacy available fixture job 삭제는 이전 pre live post group을 대상으로 한다`() {
        val registrar = registrar()
        whenever(scheduler.deleteJob(any())).thenReturn(true)

        val deletedCount = registrar.deleteLegacyAvailableFixtureJobs("fixture-1")

        assertThat(deletedCount).isEqualTo(3)
        verify(scheduler).deleteJob(JobKey.jobKey("pre-match-fixture-1", "pre-match"))
        verify(scheduler).deleteJob(JobKey.jobKey("live-match-fixture-1", "live-match"))
        verify(scheduler).deleteJob(JobKey.jobKey("post-match-fixture-1", "post-match"))
    }

    private fun registrar(): LegacyAvailableMatchJobRegistrar {
        val matchJobRegistrar = MatchJobRegistrar(scheduler)
        return LegacyAvailableMatchJobRegistrar(scheduler, matchJobRegistrar)
    }
}
