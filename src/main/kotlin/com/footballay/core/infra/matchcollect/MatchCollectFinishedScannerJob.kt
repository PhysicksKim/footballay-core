package com.footballay.core.infra.matchcollect

import com.footballay.core.logger
import org.quartz.Job
import org.quartz.JobExecutionContext

class MatchCollectFinishedScannerJob(
    private val leagueMatchCollectManager: LeagueMatchCollectManager,
) : Job {
    private val log = logger()

    override fun execute(context: JobExecutionContext) {
        val result = leagueMatchCollectManager.collectDueFinishedFixtures()
        if (result.failed > 0) {
            log.warn("FINISHED match collect scanner completed with failures - result={}", result)
            return
        }

        log.info("FINISHED match collect scanner completed - result={}", result)
    }
}
