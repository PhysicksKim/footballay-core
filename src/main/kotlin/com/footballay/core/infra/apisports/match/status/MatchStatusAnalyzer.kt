package com.footballay.core.infra.apisports.match.status

import com.footballay.core.infra.apisports.match.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.plan.MatchSyncConstants.KICKOFF_IMMINENT_THRESHOLD_MINUTES
import com.footballay.core.infra.apisports.match.plan.MatchSyncConstants.POST_MATCH_POLLING_CUTOFF_MINUTES
import com.footballay.core.infra.apisports.match.plan.dto.MatchLineupPlanDto
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class MatchStatusAnalyzer {
    private val log = logger()

    fun analyzeAndDetermineResult(dto: FullMatchSyncDto, lineupDto: MatchLineupPlanDto): MatchDataSyncResult {
        val statusShort = dto.fixture.status.short
        val kickoffTime = dto.fixture.date?.toInstant()
        val elapsedMin = dto.fixture.status.elapsed

        return when {
            isMatchFinished(statusShort) -> handleFinishedMatch(kickoffTime, elapsedMin, statusShort)
            isMatchLive(statusShort) -> handleLiveMatch(kickoffTime, elapsedMin, statusShort)
            isKickoffPassedButNotStarted(statusShort, kickoffTime) -> handleDelayedStart(kickoffTime, elapsedMin, statusShort)
            else -> handlePreMatch(kickoffTime, statusShort, lineupDto)
        }
    }

    private fun handleFinishedMatch(kickoffTime: Instant?, elapsedMin: Int?, statusShort: String): MatchDataSyncResult {
        val minutesSinceFinish = calculateMinutesSinceFinish(kickoffTime, elapsedMin)
        val shouldStopPolling = minutesSinceFinish > POST_MATCH_POLLING_CUTOFF_MINUTES

        log.info("Match finished: status={}, minutesSinceFinish={}, stopPolling={}", statusShort, minutesSinceFinish, shouldStopPolling)

        return MatchDataSyncResult.PostMatch(
            kickoffTime = kickoffTime,
            shouldStopPolling = shouldStopPolling,
            minutesSinceFinish = minutesSinceFinish,
        )
    }

    private fun handleLiveMatch(kickoffTime: Instant?, elapsedMin: Int?, statusShort: String): MatchDataSyncResult {
        log.info("Match live: status={}, elapsed={}min", statusShort, elapsedMin)

        return MatchDataSyncResult.Live(
            kickoffTime = kickoffTime,
            isMatchFinished = false,
            elapsedMin = elapsedMin,
            statusShort = statusShort,
        )
    }

    private fun handleDelayedStart(kickoffTime: Instant?, elapsedMin: Int?, statusShort: String): MatchDataSyncResult {
        log.info("Kickoff passed but status=NS, treating as Live: kickoff={}, now={}", kickoffTime, Instant.now())

        return MatchDataSyncResult.Live(
            kickoffTime = kickoffTime,
            isMatchFinished = false,
            elapsedMin = elapsedMin,
            statusShort = statusShort,
        )
    }

    private fun handlePreMatch(kickoffTime: Instant?, statusShort: String, lineupDto: MatchLineupPlanDto): MatchDataSyncResult {
        val hasLineup = !lineupDto.isEmpty()
        val hasCompleteLineup = hasLineup && lineupDto.hasCompleteLineup()
        val isKickoffImminent = isKickoffWithinMinutes(kickoffTime, KICKOFF_IMMINENT_THRESHOLD_MINUTES)
        val shouldTerminatePreMatchJob = hasCompleteLineup || isKickoffImminent

        log.info(
            "Pre-match: status={}, lineup={}, complete={}, imminent={}, terminate={}",
            statusShort, hasLineup, hasCompleteLineup, isKickoffImminent, shouldTerminatePreMatchJob
        )

        return MatchDataSyncResult.PreMatch(
            lineupCached = hasLineup,
            kickoffTime = kickoffTime,
            shouldTerminatePreMatchJob = shouldTerminatePreMatchJob,
        )
    }

    private fun isMatchFinished(statusShort: String): Boolean = statusShort in FINISHED_STATUSES

    private fun isMatchLive(statusShort: String): Boolean = statusShort in LIVE_STATUSES

    private fun isKickoffPassedButNotStarted(statusShort: String, kickoffTime: Instant?): Boolean {
        return statusShort == "NS" && isKickoffPassed(kickoffTime)
    }

    private fun isKickoffPassed(kickoffTime: Instant?): Boolean {
        return kickoffTime?.let { Instant.now().isAfter(it) } ?: false
    }

    private fun isKickoffWithinMinutes(kickoffTime: Instant?, minutes: Long): Boolean {
        if (kickoffTime == null) return false
        val minutesUntilKickoff = Duration.between(Instant.now(), kickoffTime).toMinutes()
        return minutesUntilKickoff <= minutes
    }

    private fun calculateMinutesSinceFinish(kickoffTime: Instant?, elapsedMin: Int?): Long {
        if (kickoffTime == null || elapsedMin == null) return 0L
        val matchEndTime = kickoffTime.plusSeconds(elapsedMin.toLong() * 60)
        return Duration.between(matchEndTime, Instant.now()).toMinutes().coerceAtLeast(0)
    }

    companion object {
        private val FINISHED_STATUSES = setOf("FT", "AET", "PEN", "AWD", "WO", "CANC", "PST", "ABD")
        private val LIVE_STATUSES = setOf("1H", "HT", "2H", "ET", "BT", "P", "SUSP", "LIVE")
    }
}
