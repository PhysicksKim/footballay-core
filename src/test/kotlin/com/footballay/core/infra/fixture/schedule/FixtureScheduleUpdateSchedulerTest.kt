package com.footballay.core.infra.fixture.schedule

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.quartz.CronTrigger
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.Trigger

@ExtendWith(MockitoExtension::class)
class FixtureScheduleUpdateSchedulerTest {
    @Mock
    private lateinit var scheduler: Scheduler

    @Test
    fun `hourly durable batch job을 per-fixture match group과 분리된 group에 등록한다`() {
        val service = FixtureScheduleUpdateScheduler(scheduler)
        val jobCaptor = argumentCaptor<JobDetail>()
        val triggerCaptor = argumentCaptor<Trigger>()
        whenever(scheduler.checkExists(FixtureScheduleUpdateScheduler.JOB_KEY)).thenReturn(false)

        val result = service.registerHourlyJob()

        assertThat(result).isTrue()
        verify(scheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture())

        val job = jobCaptor.firstValue
        val trigger = triggerCaptor.firstValue as CronTrigger
        assertThat(job.key).isEqualTo(FixtureScheduleUpdateScheduler.JOB_KEY)
        assertThat(job.isDurable).isTrue()
        assertThat(job.jobClass).isEqualTo(FixtureScheduleUpdateJob::class.java)
        assertThat(trigger.key).isEqualTo(FixtureScheduleUpdateScheduler.TRIGGER_KEY)
        assertThat(trigger.jobKey).isEqualTo(FixtureScheduleUpdateScheduler.JOB_KEY)
        assertThat(trigger.cronExpression).isEqualTo(FixtureScheduleUpdateScheduler.HOURLY_CRON)
        assertThat(FixtureScheduleUpdateScheduler.GROUP).isEqualTo("batch:fixture-schedule")
    }

    @Test
    fun `기존 batch job이 있으면 삭제 후 다시 등록한다`() {
        val service = FixtureScheduleUpdateScheduler(scheduler)
        whenever(scheduler.checkExists(FixtureScheduleUpdateScheduler.JOB_KEY)).thenReturn(true)

        val result = service.registerHourlyJob()

        assertThat(result).isTrue()
        verify(scheduler).deleteJob(FixtureScheduleUpdateScheduler.JOB_KEY)
        verify(scheduler).scheduleJob(any<JobDetail>(), any<Trigger>())
    }

    @Test
    fun `startup hook은 등록 성공 시 등록된 Quartz job을 즉시 trigger한다`() {
        val service = FixtureScheduleUpdateScheduler(scheduler)
        whenever(scheduler.checkExists(FixtureScheduleUpdateScheduler.JOB_KEY)).thenReturn(false)

        service.registerAndTriggerOnStartup()

        verify(scheduler).scheduleJob(any<JobDetail>(), any<Trigger>())
        verify(scheduler).triggerJob(FixtureScheduleUpdateScheduler.JOB_KEY)
    }

    @Test
    fun `등록 실패 시 startup trigger를 실행하지 않는다`() {
        val service = FixtureScheduleUpdateScheduler(scheduler)
        whenever(scheduler.checkExists(FixtureScheduleUpdateScheduler.JOB_KEY)).thenThrow(RuntimeException("quartz failed"))

        service.registerAndTriggerOnStartup()

        verify(scheduler, never()).triggerJob(FixtureScheduleUpdateScheduler.JOB_KEY)
    }
}
