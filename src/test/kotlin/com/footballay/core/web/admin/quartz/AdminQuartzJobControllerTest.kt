package com.footballay.core.web.admin.quartz

import com.footballay.core.infra.scheduler.PreMatchJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.security.access.prepost.PreAuthorize
import java.time.Instant
import java.util.Date

@ExtendWith(MockitoExtension::class)
class AdminQuartzJobControllerTest {
    @Mock
    private lateinit var scheduler: Scheduler

    @Test
    fun `groupPrefix에 맞는 Quartz job과 trigger 정보를 반환한다`() {
        val controller = AdminQuartzJobController(scheduler)
        val jobKey = JobKey.jobKey("available:pre:fixture-1", "league:match:league-1")
        val trigger = simpleTrigger(jobKey)
        val jobDetail =
            JobBuilder
                .newJob(PreMatchJob::class.java)
                .withIdentity(jobKey)
                .build()
        whenever(scheduler.jobGroupNames)
            .thenReturn(listOf("league:match:league-1", "football-fixture"))
        whenever(scheduler.getJobKeys(any<GroupMatcher<JobKey>>()))
            .thenReturn(setOf(jobKey))
        whenever(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail)
        whenever(scheduler.getTriggersOfJob(jobKey)).thenReturn(listOf(trigger))
        whenever(scheduler.getTriggerState(trigger.key)).thenReturn(Trigger.TriggerState.NORMAL)

        val result = controller.getJobs(groupPrefix = "league:match:")

        assertThat(result).hasSize(1)
        val job = result.first()
        assertThat(job.jobName).isEqualTo("available:pre:fixture-1")
        assertThat(job.jobGroup).isEqualTo("league:match:league-1")
        assertThat(job.jobClass).isEqualTo(PreMatchJob::class.java.name)
        assertThat(job.parsedIdentity)
            .isEqualTo(
                AdminQuartzMatchJobIdentityResponse(
                    owner = "AVAILABLE",
                    phase = "PRE",
                    leagueUid = "league-1",
                    fixtureUid = "fixture-1",
                ),
            )
        assertThat(job.triggers).hasSize(1)
        assertThat(job.triggers.first().triggerState).isEqualTo("NORMAL")
        assertThat(job.triggers.first().repeatIntervalMillis).isEqualTo(60_000L)
        assertThat(job.triggers.first().repeatCount).isEqualTo(300)
        assertThat(job.triggers.first().timesTriggered).isZero()
    }

    @Test
    fun `groupPrefix에 맞지 않는 group은 조회하지 않는다`() {
        val controller = AdminQuartzJobController(scheduler)
        whenever(scheduler.jobGroupNames)
            .thenReturn(listOf("football-fixture"))

        val result = controller.getJobs(groupPrefix = "league:match:")

        assertThat(result).isEmpty()
        verify(scheduler, never()).getJobKeys(any<GroupMatcher<JobKey>>())
    }

    @Test
    fun `current match job 규칙이 아니면 parsedIdentity는 null이다`() {
        val controller = AdminQuartzJobController(scheduler)
        val jobKey = JobKey.jobKey("pre-match-fixture-1", "pre-match")
        whenever(scheduler.jobGroupNames).thenReturn(listOf("pre-match"))
        whenever(scheduler.getJobKeys(any<GroupMatcher<JobKey>>())).thenReturn(setOf(jobKey))
        whenever(scheduler.getJobDetail(jobKey)).thenReturn(null)
        whenever(scheduler.getTriggersOfJob(jobKey)).thenReturn(emptyList())

        val result = controller.getJobs(groupPrefix = null)

        assertThat(result).hasSize(1)
        assertThat(result.first().jobName).isEqualTo("pre-match-fixture-1")
        assertThat(result.first().jobGroup).isEqualTo("pre-match")
        assertThat(result.first().parsedIdentity).isNull()
        assertThat(result.first().triggers).isEmpty()
    }

    @Test
    fun `Quartz 조회 API는 ADMIN 권한을 요구한다`() {
        val annotation = AdminQuartzJobController::class.java.getAnnotation(PreAuthorize::class.java)

        assertThat(annotation).isNotNull
        assertThat(annotation.value).isEqualTo("hasRole('ADMIN')")
    }

    private fun simpleTrigger(jobKey: JobKey): org.quartz.SimpleTrigger =
        TriggerBuilder
            .newTrigger()
            .withIdentity("${jobKey.name}:trigger", jobKey.group)
            .forJob(jobKey)
            .startAt(Date.from(Instant.parse("2026-05-25T12:00:00Z")))
            .withSchedule(
                SimpleScheduleBuilder
                    .simpleSchedule()
                    .withIntervalInSeconds(60)
                    .withRepeatCount(300),
            ).build() as org.quartz.SimpleTrigger
}
