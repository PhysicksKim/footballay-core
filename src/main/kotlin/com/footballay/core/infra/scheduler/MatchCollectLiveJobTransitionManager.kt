package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.scheduler.matchjob.MatchJobKeyFactory
import com.footballay.core.infra.scheduler.matchjob.MatchJobOwner
import com.footballay.core.infra.scheduler.matchjob.MatchJobPhase
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class MatchCollectLiveJobTransitionManager(
    private val jobSchedulerService: JobSchedulerService,
) {
    private val log = logger()

    fun handle(
        fixtureUid: String,
        result: MatchDataSyncResult,
        context: MatchCollectLiveJobContext,
    ) {
        when (result) {
            is MatchDataSyncResult.PreMatch -> handlePreMatch(fixtureUid, result, context)
            is MatchDataSyncResult.Live -> handleLive(fixtureUid, context)
            is MatchDataSyncResult.PostMatch -> handlePostMatch(fixtureUid, result, context)
            is MatchDataSyncResult.NotPlayed -> handleNotPlayed(fixtureUid, result, context)
            is MatchDataSyncResult.Error -> {
                log.warn(
                    "MatchCollect LIVE sync error - fixtureUid={}, phase={}, message={}",
                    fixtureUid,
                    context.phase,
                    result.message,
                )
            }
        }
    }

    private fun handlePreMatch(
        fixtureUid: String,
        result: MatchDataSyncResult.PreMatch,
        context: MatchCollectLiveJobContext,
    ) {
        if (context.phase != MatchCollectLiveJobPhase.PRE) {
            log.warn("PreMatch result received from non-pre matchcollect job - fixtureUid={}, phase={}", fixtureUid, context.phase)
            return
        }

        if (result.shouldTerminatePreMatchJob) {
            jobSchedulerService.removeJob(context.jobKey)
        }
    }

    private fun handleLive(
        fixtureUid: String,
        context: MatchCollectLiveJobContext,
    ) {
        if (context.phase != MatchCollectLiveJobPhase.LIVE) {
            log.warn("Live result received from non-live matchcollect job - fixtureUid={}, phase={}", fixtureUid, context.phase)
        }
    }

    private fun handlePostMatch(
        fixtureUid: String,
        result: MatchDataSyncResult.PostMatch,
        context: MatchCollectLiveJobContext,
    ) {
        if (context.phase == MatchCollectLiveJobPhase.LIVE) {
            transitionLiveToPost(fixtureUid, context)
            return
        }

        if (context.phase != MatchCollectLiveJobPhase.POST) {
            log.warn("PostMatch result received from non-post matchcollect job - fixtureUid={}, phase={}", fixtureUid, context.phase)
            return
        }

        if (result.shouldStopPolling) {
            jobSchedulerService.removeJob(context.jobKey)
        }
    }

    private fun transitionLiveToPost(
        fixtureUid: String,
        context: MatchCollectLiveJobContext,
    ) {
        val currentJobIdentity = MatchJobKeyFactory.parseJobKey(context.jobKey)
        if (currentJobIdentity == null) {
            log.warn("Cannot parse matchcollect live job key - fixtureUid={}, jobKey={}", fixtureUid, context.jobKey)
            return
        }

        jobSchedulerService.removeJob(context.jobKey)
        jobSchedulerService.registerOrReplaceMatchCollectJob(
            phase = MatchJobPhase.POST,
            leagueUid = currentJobIdentity.leagueUid,
            fixtureUid = fixtureUid,
            startTime = Instant.now(),
            compareStartAt = false,
        )
    }

    private fun handleNotPlayed(
        fixtureUid: String,
        result: MatchDataSyncResult.NotPlayed,
        context: MatchCollectLiveJobContext,
    ) {
        val currentJobIdentity = MatchJobKeyFactory.parseJobKey(context.jobKey)
        if (currentJobIdentity == null) {
            log.warn(
                "Cannot parse matchcollect job key for not played cleanup - fixtureUid={}, status={}, jobKey={}",
                fixtureUid,
                result.statusCode,
                context.jobKey,
            )
            return
        }

        jobSchedulerService.deleteFixtureJobs(
            leagueUid = currentJobIdentity.leagueUid,
            fixtureUid = fixtureUid,
            owner = MatchJobOwner.MATCHCOLLECT,
        )
    }
}
