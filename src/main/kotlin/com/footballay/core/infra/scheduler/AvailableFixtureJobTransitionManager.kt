package com.footballay.core.infra.scheduler

import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.scheduler.matchjob.MatchJobKeyFactory
import com.footballay.core.infra.scheduler.matchjob.MatchJobIdentity
import com.footballay.core.infra.scheduler.matchjob.MatchJobOwner
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
            is MatchDataSyncResult.PreMatch -> {
                handlePreMatch(fixtureUid, result, context)
            }

            is MatchDataSyncResult.Live -> {
                handleLive(fixtureUid, context)
            }

            is MatchDataSyncResult.PostMatch -> {
                handlePostMatch(fixtureUid, result, context)
            }

            is MatchDataSyncResult.NotPlayed -> {
                handleNotPlayed(fixtureUid, result, context)
            }

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
        val currentJobIdentity = currentMatchJobIdentityOf(context)
        if (currentJobIdentity != null) {
            jobSchedulerService.addPostMatchJob(
                leagueUid = currentJobIdentity.leagueUid,
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
        val currentJobIdentity = currentMatchJobIdentityOf(context)
        if (currentJobIdentity != null) {
            jobSchedulerService.deleteFixtureJobs(
                leagueUid = currentJobIdentity.leagueUid,
                fixtureUid = fixtureUid,
                owner = MatchJobOwner.AVAILABLE,
            )
        } else {
            jobSchedulerService.deleteLegacyAvailableFixtureJobs(fixtureUid)
        }
    }

    /**
     * 현재 naming 규칙의 jobKey 는 `league:match:{leagueUid}` group 과 `{owner}:{phase}:{fixtureUid}` name 을 가집니다.
     *
     * naming이 현재 지원하는 방식과 다르다면 (예를 들어 legacy) null을 반환합니다.
     * 파싱에 실패하면 이전 `pre-match/live-match/post-match` group 으로 등록된 legacy job 으로 보고 fallback 경로를 사용합니다.
     */
    private fun currentMatchJobIdentityOf(context: AvailableFixtureJobContext): MatchJobIdentity? = MatchJobKeyFactory.parseJobKey(context.jobKey)
}
