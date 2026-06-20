package com.footballay.core.infra.scheduler.matchjob

import com.footballay.core.infra.scheduler.MatchCollectLiveFixtureJob
import com.footballay.core.infra.scheduler.MatchCollectPostFixtureJob
import com.footballay.core.infra.scheduler.MatchCollectPreFixtureJob
import java.time.Instant

object MatchCollectMatchJobSchedulePolicy {
    private const val PRE_MATCH_INTERVAL_SECONDS = 120
    private const val PRE_MATCH_MAX_EXECUTIONS = 30

    private const val LIVE_MATCH_INTERVAL_SECONDS = 60
    private const val LIVE_MATCH_MAX_EXECUTIONS = 240

    private const val POST_MATCH_INTERVAL_SECONDS = 300
    private const val POST_MATCH_MAX_EXECUTIONS = 12

    fun schedule(
        phase: MatchJobPhase,
        startAt: Instant,
        compareStartAt: Boolean = true,
    ): MatchJobSchedule =
        when (phase) {
            MatchJobPhase.PRE ->
                MatchJobSchedule(
                    jobClass = MatchCollectPreFixtureJob::class.java,
                    startAt = startAt,
                    repeatIntervalSeconds = PRE_MATCH_INTERVAL_SECONDS,
                    repeatCount = PRE_MATCH_MAX_EXECUTIONS,
                    compareStartAt = compareStartAt,
                )

            MatchJobPhase.LIVE ->
                MatchJobSchedule(
                    jobClass = MatchCollectLiveFixtureJob::class.java,
                    startAt = startAt,
                    repeatIntervalSeconds = LIVE_MATCH_INTERVAL_SECONDS,
                    repeatCount = LIVE_MATCH_MAX_EXECUTIONS,
                    compareStartAt = compareStartAt,
                )

            MatchJobPhase.POST ->
                MatchJobSchedule(
                    jobClass = MatchCollectPostFixtureJob::class.java,
                    startAt = startAt,
                    repeatIntervalSeconds = POST_MATCH_INTERVAL_SECONDS,
                    repeatCount = POST_MATCH_MAX_EXECUTIONS,
                    compareStartAt = compareStartAt,
                )
        }
}
