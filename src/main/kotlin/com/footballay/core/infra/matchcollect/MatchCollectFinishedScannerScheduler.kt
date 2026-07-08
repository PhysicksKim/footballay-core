package com.footballay.core.infra.matchcollect

import com.footballay.core.infra.scheduler.config.MatchCollectProperties
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

@Component
@ConditionalOnProperty(
    prefix = MatchCollectProperties.PREFIX,
    name = [MatchCollectProperties.FINISHED_SCANNER_ENABLED],
    havingValue = "true",
    matchIfMissing = true,
)
class MatchCollectFinishedScannerScheduler(
    private val scheduler: Scheduler,
) {
    private val log = logger()

    @Order(110)
    @EventListener(ApplicationReadyEvent::class)
    fun registerOnStartup() {
        registerCronJob()
    }

    fun registerCronJob(): Boolean =
        try {
            val job =
                JobBuilder
                    .newJob(MatchCollectFinishedScannerJob::class.java)
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
                            .cronSchedule(THIRTY_MINUTE_CRON)
                            .withMisfireHandlingInstructionDoNothing(),
                    ).build()

            if (scheduler.checkExists(JOB_KEY)) {
                scheduler.deleteJob(JOB_KEY)
            }

            scheduler.scheduleJob(job, trigger)
            log.info("FINISHED match collect scanner job registered - jobKey={}, triggerKey={}", JOB_KEY, TRIGGER_KEY)
            true
        } catch (e: Exception) {
            log.error("Failed to register FINISHED match collect scanner job", e)
            false
        }

    companion object {
        const val GROUP = "batch:match-collect"
        const val JOB_NAME = "finished-scanner"
        const val TRIGGER_NAME = "finished-scanner:cron"
        const val THIRTY_MINUTE_CRON = "0 0/30 * * * ?"

        val JOB_KEY: JobKey = JobKey.jobKey(JOB_NAME, GROUP)
        val TRIGGER_KEY: TriggerKey = TriggerKey.triggerKey(TRIGGER_NAME, GROUP)
    }
}
