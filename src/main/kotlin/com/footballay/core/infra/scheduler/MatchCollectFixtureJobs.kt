package com.footballay.core.infra.scheduler

import com.footballay.core.infra.matchcollect.MatchCollectSyncExecutor
import com.footballay.core.logger
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import java.time.Clock
import java.time.Instant

class MatchCollectPreFixtureJob(
    private val executor: MatchCollectSyncExecutor,
    private val clock: Clock = Clock.systemUTC(),
) : Job {
    private val log = logger()

    override fun execute(context: JobExecutionContext) {
        val fixtureUid = fixtureUidOf(context, "MatchCollectPreFixtureJob")
        val now = Instant.now(clock)
        val result = executor.collectPre(fixtureUid, now)
        log.info("MatchCollectPreFixtureJob completed - fixtureUid={}, result={}", fixtureUid, result)
    }
}

class MatchCollectLiveFixtureJob(
    private val executor: MatchCollectSyncExecutor,
    private val clock: Clock = Clock.systemUTC(),
) : Job {
    private val log = logger()

    override fun execute(context: JobExecutionContext) {
        val fixtureUid = fixtureUidOf(context, "MatchCollectLiveFixtureJob")
        val now = Instant.now(clock)
        val result = executor.collectLive(fixtureUid, now)
        log.info("MatchCollectLiveFixtureJob completed - fixtureUid={}, result={}", fixtureUid, result)
    }
}

class MatchCollectPostFixtureJob(
    private val executor: MatchCollectSyncExecutor,
    private val clock: Clock = Clock.systemUTC(),
) : Job {
    private val log = logger()

    override fun execute(context: JobExecutionContext) {
        val fixtureUid = fixtureUidOf(context, "MatchCollectPostFixtureJob")
        val now = Instant.now(clock)
        val result = executor.collectPost(fixtureUid, now)
        log.info("MatchCollectPostFixtureJob completed - fixtureUid={}, result={}", fixtureUid, result)
    }
}

private fun fixtureUidOf(
    context: JobExecutionContext,
    jobName: String,
): String {
    val fixtureUid = context.mergedJobDataMap.getString(JobSchedulerService.KEY_FIXTURE_UID)
    if (fixtureUid.isNullOrBlank()) {
        throw JobExecutionException("$jobName requires fixtureUid")
    }
    return fixtureUid
}
