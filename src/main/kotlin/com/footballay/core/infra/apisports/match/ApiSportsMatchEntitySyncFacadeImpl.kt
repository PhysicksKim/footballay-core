package com.footballay.core.infra.apisports.match

import com.footballay.core.infra.apisports.match.plan.MatchSyncConstants.KICKOFF_IMMINENT_THRESHOLD_MINUTES
import com.footballay.core.infra.apisports.match.plan.MatchSyncConstants.POST_MATCH_POLLING_CUTOFF_MINUTES
import com.footballay.core.infra.apisports.match.plan.base.MatchBaseDtoExtractor
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.dto.MatchLineupPlanDto
import com.footballay.core.infra.apisports.match.plan.event.MatchEventDtoExtractor
import com.footballay.core.infra.apisports.match.plan.lineup.MatchLineupDtoExtractor
import com.footballay.core.infra.apisports.match.plan.playerstat.MatchPlayerStatDtoExtractor
import com.footballay.core.infra.apisports.match.plan.teamstat.MatchTeamStatDtoExtractor
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.logger
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

/**
 * ApiSports Match Data Sync Facade implementation
 *
 * JPA 엔티티 저장과 계획을 분리하기 위해서 Facade 에서 이미 계획을 세우고, 세워진 계획을 엔티티 저장 서비스에 위임합니다.
 *
 * Match Data 전체를 담은 [FullMatchSyncDto] 를 각각 Match 엔티티에 알맞게 추려냅니다.
 * [MatchPlayerContext] 는 FullDto 에 등장한 선수들을 추려내서 관리하는 역할을 합니다.
 * 엔티티 저장 책임은 [MatchEntityPersistManager] 에게 위임하며
 * 추려낸 Plan(DTO) 들과 [MatchPlayerContext] 를 전달합니다.
 *
 * ### MatchPlayerDto 추출 중요성
 * ApiSports 는 불안정한 데이터 제공으로 인해 선수 아이디가 누락되는 경우가 빈번합니다.
 * 따라서 [FullMatchSyncDto] 를 분해하는 작업과 함께 [MatchPlayerContext] 를 구성하여서
 * Match 에 등장한 MatchPlayer 들을 확보하는 작업을 통해, 최대한 id null 문제를 완화하려고 합니다.
 *
 * 다만 id=null 선수에 대한 Name 기반 매칭은, Lineup 과 Statistics 에서 이름 표기가 달라서 매칭 성공률은 높지 않습니다.
 * 즉, id=null 선수는 Lineup,Event,Statistics 모두 다 이름이 다르게 나올 수 있으며,
 * 경험적으로 statistics 에서 이름이 다르게 나올 확률이 높습니다.
 *
 * @see MatchEntityPersistManager
 * @see MatchPlayerContext
 * @see FullMatchSyncDto
 */
@Service
class ApiSportsMatchEntitySyncFacadeImpl(
    // DTO 추출기들 - Plan 생성 책임
    private val baseDtoExtractor: MatchBaseDtoExtractor,
    private val lineupDtoExtractor: MatchLineupDtoExtractor,
    private val eventDtoExtractor: MatchEventDtoExtractor,
    private val teamStatExtractor: MatchTeamStatDtoExtractor,
    private val playerStatExtractor: MatchPlayerStatDtoExtractor,
    // 엔티티 저장 관리자 - Plan to Entity 책임
    private val matchEntityPersistManager: MatchEntityPersistManager,
) : ApiSportsMatchEntitySyncFacade {
    private val log = logger()

    override fun syncFixtureMatchEntities(dto: FullMatchSyncDto): MatchDataSyncResult {
        val fixtureApiId = dto.fixture.id
        log.info("Starting match data sync for fixtureApiId={}", fixtureApiId)

        try {
            val context = MatchPlayerContext()

            // DTO 추출
            val baseDto = baseDtoExtractor.extractBaseMatch(dto)
            val lineupDto = lineupDtoExtractor.extractLineup(dto, context)
            val eventDto = eventDtoExtractor.extractEvents(dto, context)
            val teamStatDto = teamStatExtractor.extractTeamStats(dto)
            val playerStatDto = playerStatExtractor.extractPlayerStats(dto, context)

            log.info(
                "Extracted DTOs - Lineup: ${context.lineupMpDtoMap.size}, Event: ${context.eventMpDtoMap.size}, Stat: ${context.statMpDtoMap.size}",
            )

            // 엔티티 동기화 (트랜잭션)
            val syncResult =
                matchEntityPersistManager.syncMatchEntities(
                    fixtureApiId = fixtureApiId,
                    baseDto = baseDto,
                    lineupDto = lineupDto,
                    eventDto = eventDto,
                    teamStatDto = teamStatDto,
                    playerStatDto = playerStatDto,
                    playerContext = context,
                )

            log.info(
                "Match sync completed - Created: ${syncResult.createdCount}, Retained: ${syncResult.retainedCount}, Deleted: ${syncResult.deletedCount}",
            )

            // 경기 상태에 따른 상세 Result 반환
            return determineMatchDataSyncResult(dto, lineupDto)
        } catch (e: Exception) {
            log.error("Failed to sync match data for fixture: {}", fixtureApiId, e)
            return MatchDataSyncResult.Error(
                message = "Match data sync failed: ${e.message}",
                kickoffTime = dto.fixture.date?.toInstant(),
            )
        }
    }

    /**
     * 경기 상태를 분석하여 적절한 MatchDataSyncResult를 결정합니다.
     *
     * **상태 판단 기준:**
     * - **PreMatch** (NS, TBD, INT): 경기 전 단계
     *   - shouldTerminatePreMatchJob: 완전한 라인업 (모든 선수 ID 존재) OR 킥오프 1분 전
     * - **Live** (1H, HT, 2H, ET, BT, P, SUSP, LIVE): 경기 진행 중
     * - **PostMatch** (FT, AET, PEN): 경기 종료 후
     *   - shouldStopPolling: 경기 종료 후 60분 경과
     *
     * @param dto Match 데이터 DTO
     * @param lineupDto 라인업 데이터
     */
    private fun determineMatchDataSyncResult(
        dto: FullMatchSyncDto,
        lineupDto: MatchLineupPlanDto,
    ): MatchDataSyncResult {
        val statusShort = dto.fixture.status.short
        val kickoffTime = dto.fixture.date?.toInstant()
        val elapsedMin = dto.fixture.status.elapsed

        return when {
            // 경기 종료 상태
            isMatchFinished(statusShort) -> {
                val minutesSinceFinish = calculateMinutesSinceFinish(kickoffTime, elapsedMin)
                val shouldStopPolling = minutesSinceFinish > POST_MATCH_POLLING_CUTOFF_MINUTES

                log.info(
                    "Match finished - status={}, minutesSinceFinish={}, shouldStopPolling={}",
                    statusShort,
                    minutesSinceFinish,
                    shouldStopPolling,
                )

                MatchDataSyncResult.PostMatch(
                    kickoffTime = kickoffTime,
                    shouldStopPolling = shouldStopPolling,
                    minutesSinceFinish = minutesSinceFinish,
                )
            }

            // 경기 진행 중
            isMatchLive(statusShort) -> {
                log.info("Match is live - status={}, elapsed={}", statusShort, elapsedMin)

                MatchDataSyncResult.Live(
                    kickoffTime = kickoffTime,
                    isMatchFinished = false,
                    elapsedMin = elapsedMin,
                    statusShort = statusShort,
                )
            }

            // 경기 전 단계
            else -> {
                val hasLineup = !lineupDto.isEmpty()
                val hasCompleteLineup = hasLineup && lineupDto.hasCompleteLineup()
                val isKickoffImminent = isKickoffWithinMinutes(kickoffTime, KICKOFF_IMMINENT_THRESHOLD_MINUTES)
                val shouldTerminatePreMatchJob = hasCompleteLineup || isKickoffImminent

                log.info(
                    "Pre-match phase - status={}, lineupCached={}, hasCompleteLineup={}, isKickoffImminent={}, shouldTerminatePreMatchJob={}",
                    statusShort,
                    hasLineup,
                    hasCompleteLineup,
                    isKickoffImminent,
                    shouldTerminatePreMatchJob,
                )

                MatchDataSyncResult.PreMatch(
                    lineupCached = hasLineup,
                    kickoffTime = kickoffTime,
                    shouldTerminatePreMatchJob = shouldTerminatePreMatchJob,
                )
            }
        }
    }

    /**
     * 경기 종료 상태인지 확인
     */
    private fun isMatchFinished(statusShort: String): Boolean = statusShort in FINISHED_STATUSES

    /**
     * 경기 진행 중 상태인지 확인
     */
    private fun isMatchLive(statusShort: String): Boolean = statusShort in LIVE_STATUSES

    /**
     * 킥오프까지 남은 시간이 지정된 분(minute) 이내인지 확인
     *
     * @param kickoffTime 킥오프 시각
     * @param minutes 임박 판단 기준 시간 (분)
     * @return 킥오프까지 minutes 이내이면 true
     */
    private fun isKickoffWithinMinutes(
        kickoffTime: Instant?,
        minutes: Long,
    ): Boolean {
        if (kickoffTime == null) return false
        val now = Instant.now()
        val minutesUntilKickoff = Duration.between(now, kickoffTime).toMinutes()
        return minutesUntilKickoff <= minutes
    }

    /**
     * 경기 종료 후 경과 시간 계산 (분 단위)
     */
    private fun calculateMinutesSinceFinish(
        kickoffTime: Instant?,
        elapsedMin: Int?,
    ): Long {
        if (kickoffTime == null || elapsedMin == null) {
            return 0L
        }

        val now = Instant.now()
        val matchEndTime = kickoffTime.plusSeconds(elapsedMin.toLong() * 60)
        return Duration.between(matchEndTime, now).toMinutes().coerceAtLeast(0)
    }

    companion object {
        // 경기 종료 상태 코드
        private val FINISHED_STATUSES = setOf("FT", "AET", "PEN", "AWD", "WO", "CANC", "PST", "ABD")

        // 경기 진행 중 상태 코드
        private val LIVE_STATUSES = setOf("1H", "HT", "2H", "ET", "BT", "P", "SUSP", "LIVE")
    }
}
