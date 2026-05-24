package com.footballay.core.infra.scheduler.matchjob

import com.footballay.core.infra.scheduler.LiveMatchJob
import com.footballay.core.infra.scheduler.PostMatchJob
import com.footballay.core.infra.scheduler.PreMatchJob
import java.time.Instant

object AvailableMatchJobSchedulePolicy {
    private const val PRE_MATCH_INTERVAL_SECONDS = 60
    private const val PRE_MATCH_MAX_EXECUTIONS = 300

    private const val LIVE_MATCH_INTERVAL_SECONDS = 17
    private const val LIVE_MATCH_MAX_EXECUTIONS = 1058

    private const val POST_MATCH_INTERVAL_SECONDS = 60
    private const val POST_MATCH_MAX_EXECUTIONS = 60

    fun schedule(
        phase: MatchJobPhase,
        startAt: Instant,
        compareStartAt: Boolean = true,
    ): MatchJobSchedule =
        when (phase) {
            MatchJobPhase.PRE ->
                MatchJobSchedule(
                    jobClass = PreMatchJob::class.java,
                    startAt = startAt,
                    repeatIntervalSeconds = PRE_MATCH_INTERVAL_SECONDS,
                    repeatCount = PRE_MATCH_MAX_EXECUTIONS,
                    compareStartAt = compareStartAt,
                )

            MatchJobPhase.LIVE ->
                MatchJobSchedule(
                    jobClass = LiveMatchJob::class.java,
                    startAt = startAt,
                    repeatIntervalSeconds = LIVE_MATCH_INTERVAL_SECONDS,
                    repeatCount = LIVE_MATCH_MAX_EXECUTIONS,
                    compareStartAt = compareStartAt,
                )

            MatchJobPhase.POST ->
                MatchJobSchedule(
                    jobClass = PostMatchJob::class.java,
                    startAt = startAt,
                    repeatIntervalSeconds = POST_MATCH_INTERVAL_SECONDS,
                    repeatCount = POST_MATCH_MAX_EXECUTIONS,
                    compareStartAt = compareStartAt,
                )
        }
}
