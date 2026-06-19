package com.footballay.core.infra.matchcollect

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
class MatchCollectFinishedScannerSchedulerTest {
    @Mock
    private lateinit var scheduler: Scheduler

    @Test
    fun `thirty minute durable scanner job을 batch group에 등록한다`() {
        val service = MatchCollectFinishedScannerScheduler(scheduler)
        val jobCaptor = argumentCaptor<JobDetail>()
        val triggerCaptor = argumentCaptor<Trigger>()
        whenever(scheduler.checkExists(MatchCollectFinishedScannerScheduler.JOB_KEY)).thenReturn(false)

        val result = service.registerCronJob()

        assertThat(result).isTrue()
        verify(scheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture())

        val job = jobCaptor.firstValue
        val trigger = triggerCaptor.firstValue as CronTrigger
        assertThat(job.key).isEqualTo(MatchCollectFinishedScannerScheduler.JOB_KEY)
        assertThat(job.isDurable).isTrue()
        assertThat(job.jobClass).isEqualTo(MatchCollectFinishedScannerJob::class.java)
        assertThat(trigger.key).isEqualTo(MatchCollectFinishedScannerScheduler.TRIGGER_KEY)
        assertThat(trigger.jobKey).isEqualTo(MatchCollectFinishedScannerScheduler.JOB_KEY)
        assertThat(trigger.cronExpression).isEqualTo(MatchCollectFinishedScannerScheduler.THIRTY_MINUTE_CRON)
        assertThat(MatchCollectFinishedScannerScheduler.GROUP).isEqualTo("batch:match-collect")
    }

    @Test
    fun `기존 scanner job이 있으면 삭제 후 다시 등록한다`() {
        val service = MatchCollectFinishedScannerScheduler(scheduler)
        whenever(scheduler.checkExists(MatchCollectFinishedScannerScheduler.JOB_KEY)).thenReturn(true)

        val result = service.registerCronJob()

        assertThat(result).isTrue()
        verify(scheduler).deleteJob(MatchCollectFinishedScannerScheduler.JOB_KEY)
        verify(scheduler).scheduleJob(any<JobDetail>(), any<Trigger>())
    }

    @Test
    fun `startup hook은 등록만 하고 즉시 trigger하지 않는다`() {
        val service = MatchCollectFinishedScannerScheduler(scheduler)
        whenever(scheduler.checkExists(MatchCollectFinishedScannerScheduler.JOB_KEY)).thenReturn(false)

        service.registerOnStartup()

        verify(scheduler).scheduleJob(any<JobDetail>(), any<Trigger>())
        verify(scheduler, never()).triggerJob(MatchCollectFinishedScannerScheduler.JOB_KEY)
    }

    @Test
    fun `등록 실패 시 false를 반환하고 예외를 삼킨다`() {
        val service = MatchCollectFinishedScannerScheduler(scheduler)
        whenever(scheduler.checkExists(MatchCollectFinishedScannerScheduler.JOB_KEY)).thenThrow(RuntimeException("quartz failed"))

        val result = service.registerCronJob()

        assertThat(result).isFalse()
    }
}
