package com.footballay.core.infra.fixture.schedule

import com.footballay.core.infra.scheduler.config.FixtureScheduleUpdateProperties
import com.footballay.core.logger
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Fixture schedule update batch 를 Quartz cron/durable job 으로 관리합니다.
 * per-fixture match job group 과 분리된 batch group 을 사용하고, 앱 시작 시 등록된 job 을 즉시 한 번 trigger 합니다.
 * 이 job 이 무거워지거나 많은 provider 요청을 동반하게 되면 startup trigger 정책을 재검토해야 합니다.
 */
@Component
@ConditionalOnProperty(
    prefix = FixtureScheduleUpdateProperties.PREFIX,
    name = [FixtureScheduleUpdateProperties.ENABLED],
    havingValue = "true",
    matchIfMissing = true,
)
class FixtureScheduleUpdateScheduler(
    private val scheduler: Scheduler,
) {
    private val log = logger()

    @Order(100)
    @EventListener(ApplicationReadyEvent::class)
    fun registerAndTriggerOnStartup() {
        val registered = registerHourlyJob()
        if (registered) {
            triggerOnce()
        }
    }

    fun registerHourlyJob(): Boolean =
        try {
            val job =
                JobBuilder
                    .newJob(FixtureScheduleUpdateJob::class.java)
                    .withIdentity(JOB_KEY)
                    .storeDurably(true)
                    .build()

            val trigger =
                TriggerBuilder
                    .newTrigger()
                    .withIdentity(TRIGGER_KEY)
                    .forJob(JOB_KEY)
                    .withSchedule(
                        CronScheduleBuilder
                            .cronSchedule(HOURLY_CRON)
                            .withMisfireHandlingInstructionDoNothing(),
                    ).build()

            if (scheduler.checkExists(JOB_KEY)) {
                scheduler.deleteJob(JOB_KEY)
            }

            scheduler.scheduleJob(job, trigger)
            log.info("Fixture schedule update batch job registered - jobKey={}, triggerKey={}", JOB_KEY, TRIGGER_KEY)
            true
        } catch (e: Exception) {
            log.error("Failed to register fixture schedule update batch job", e)
            false
        }

    fun triggerOnce(): Boolean =
        try {
            scheduler.triggerJob(JOB_KEY)
            log.info("Fixture schedule update batch job triggered once - jobKey={}", JOB_KEY)
            true
        } catch (e: Exception) {
            log.error("Failed to trigger fixture schedule update batch job - jobKey={}", JOB_KEY, e)
            false
        }

    companion object {
        const val GROUP = "batch:fixture-schedule"
        const val JOB_NAME = "fixture-schedule-update"
        const val TRIGGER_NAME = "fixture-schedule-update:cron"
        const val HOURLY_CRON = "0 0 * * * ?"

        val JOB_KEY: JobKey = JobKey.jobKey(JOB_NAME, GROUP)
        val TRIGGER_KEY: TriggerKey = TriggerKey.triggerKey(TRIGGER_NAME, GROUP)
    }
}
