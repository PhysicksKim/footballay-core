package com.footballay.core.infra.fixture.schedule

import com.footballay.core.logger
import org.quartz.Job
import org.quartz.JobExecutionContext

/**
 * Quartz 에서 실행되는 fixture schedule update batch job 입니다.
 * available=true 리그의 fixture schedule 을 sync 하고, schedule 변경에 영향을 받는 available fixture job 을 reconcile 합니다.
 */
class FixtureScheduleUpdateJob(
    private val fixtureScheduleUpdater: FixtureScheduleUpdater,
) : Job {
    private val log = logger()

    override fun execute(context: JobExecutionContext) {
        val result = fixtureScheduleUpdater.updateAvailableLeagues()

        if (result.failedLeagues > 0 || result.reconcileFailures.isNotEmpty()) {
            log.warn("Fixture schedule update batch completed with warnings - result={}", result)
            return
        }

        log.info("Fixture schedule update batch completed - result={}", result)
    }
}
