package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class AvailableFixtureJobTransitionManager(
    private val jobSchedulerService: JobSchedulerService,
) {
    private val log = logger()

    fun handle(
        fixtureUid: String,
        result: MatchDataSyncResult,
        context: AvailableFixtureJobContext,
    ) {
        when (result) {
            is MatchDataSyncResult.PreMatch -> handlePreMatch(fixtureUid, result, context)
            is MatchDataSyncResult.Live -> handleLive(fixtureUid, context)
            is MatchDataSyncResult.PostMatch -> handlePostMatch(fixtureUid, result, context)
            is MatchDataSyncResult.NotPlayed -> handleNotPlayed(fixtureUid, result, context)
            is MatchDataSyncResult.Error -> {
                log.error(
                    "Available fixture match sync error - fixtureUid={}, phase={}, message={}",
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
        context: AvailableFixtureJobContext,
    ) {
        if (context.phase != AvailableFixtureJobPhase.PRE_MATCH) {
            log.warn(
                "PreMatch result received from non-PreMatch job - fixtureUid={}, phase={}, ignoring",
                fixtureUid,
                context.phase,
            )
            return
        }

        if (result.shouldTerminatePreMatchJob) {
            log.info("PreMatch complete - removing PreMatchJob - fixtureUid={}", fixtureUid)
            jobSchedulerService.removeJob(context.jobKey)
        }
    }

    private fun handleLive(
        fixtureUid: String,
        context: AvailableFixtureJobContext,
    ) {
        if (context.phase != AvailableFixtureJobPhase.LIVE_MATCH) {
            log.warn(
                "Live result received from non-LiveMatch job - fixtureUid={}, phase={}, ignoring",
                fixtureUid,
                context.phase,
            )
            return
        }
    }

    private fun handlePostMatch(
        fixtureUid: String,
        result: MatchDataSyncResult.PostMatch,
        context: AvailableFixtureJobContext,
    ) {
        if (context.phase == AvailableFixtureJobPhase.LIVE_MATCH) {
            transitionLiveToPost(fixtureUid, context)
            return
        }

        if (context.phase != AvailableFixtureJobPhase.POST_MATCH) {
            log.warn(
                "PostMatch result received from non-PostMatch job - fixtureUid={}, phase={}, ignoring",
                fixtureUid,
                context.phase,
            )
            return
        }

        if (result.shouldStopPolling) {
            log.info("PostMatch polling complete - removing PostMatchJob - fixtureUid={}", fixtureUid)
            jobSchedulerService.removeJob(context.jobKey)
        }
    }

    private fun transitionLiveToPost(
        fixtureUid: String,
        context: AvailableFixtureJobContext,
    ) {
        log.info("LiveMatch to PostMatch transition - fixtureUid={}", fixtureUid)
        jobSchedulerService.removeJob(context.jobKey)
        val leagueUid = MatchJobIdentity.leagueUidFromGroup(context.jobKey.group)
        if (leagueUid != null) {
            jobSchedulerService.addPostMatchJob(
                leagueUid = leagueUid,
                fixtureUid = fixtureUid,
                startTime = Instant.now(),
            )
        } else {
            @Suppress("DEPRECATION")
            jobSchedulerService.addPostMatchJob(
                fixtureUid = fixtureUid,
                startTime = Instant.now(),
            )
        }
    }

    private fun handleNotPlayed(
        fixtureUid: String,
        result: MatchDataSyncResult.NotPlayed,
        context: AvailableFixtureJobContext,
    ) {
        log.info(
            "Match not played - removing available fixture jobs - fixtureUid={}, status={}",
            fixtureUid,
            result.statusCode,
        )
        val leagueUid = MatchJobIdentity.leagueUidFromGroup(context.jobKey.group)
        if (leagueUid != null) {
            jobSchedulerService.deleteFixtureJobs(
                leagueUid = leagueUid,
                fixtureUid = fixtureUid,
                owner = MatchJobOwner.AVAILABLE,
            )
        } else {
            jobSchedulerService.removeAllJobsForFixture(fixtureUid)
        }
    }
}
