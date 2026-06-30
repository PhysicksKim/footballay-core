package com.footballay.core.infra.scheduler.matchjob

import com.footballay.core.logger
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.stereotype.Service

@Service
class MatchJobQueryService(
    private val scheduler: Scheduler,
) {
    private val log = logger()

    fun jobExists(jobKey: JobKey): Boolean =
        try {
            scheduler.checkExists(jobKey)
        } catch (e: Exception) {
            log.error("Failed to check job existence - jobKey={}", jobKey, e)
            false
        }

    fun listLeagueMatchJobs(leagueUid: String): Set<JobKey> =
        try {
            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(MatchJobKeyFactory.leagueMatchGroup(leagueUid)))
        } catch (e: Exception) {
            log.error("Failed to list league match jobs - leagueUid={}", leagueUid, e)
            emptySet()
        }
}
