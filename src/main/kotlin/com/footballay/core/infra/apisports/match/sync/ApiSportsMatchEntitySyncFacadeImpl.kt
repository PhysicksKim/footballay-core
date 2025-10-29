package com.footballay.core.infra.apisports.match.sync

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.base.MatchBaseDtoExtractor
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.event.MatchEventDtoExtractor
import com.footballay.core.infra.apisports.match.sync.lineup.MatchLineupDtoExtractor
import com.footballay.core.infra.apisports.match.sync.persist.MatchEntitySyncService
import com.footballay.core.infra.apisports.match.sync.playerstat.MatchPlayerStatDtoExtractor
import com.footballay.core.infra.apisports.match.sync.teamstat.MatchTeamStatDtoExtractor
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.logger
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime

/**
 * ApiSports Match Data Sync Facade implementation
 *
 * Match Data 전체를 담은 [FullMatchSyncDto] 를 각각 Match 엔티티에 알맞게 추려냅니다.
 * [MatchPlayerContext] 는 FullDto 에 등장한 선수들을 추려내서 관리하는 역할을 합니다.
 * 엔티티 저장 책임은 [MatchEntitySyncService] 에게 위임하며
 * 추려낸 Dto 들과 [MatchPlayerContext] 를 전달합니다.
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
 * @see MatchEntitySyncService
 * @see MatchPlayerContext
 * @see FullMatchSyncDto
 */
@Service
class ApiSportsMatchEntitySyncFacadeImpl(
    private val baseDtoExtractor: MatchBaseDtoExtractor,
    private val lineupDtoExtractor: MatchLineupDtoExtractor,
    private val eventDtoExtractor: MatchEventDtoExtractor,
    private val teamStatExtractor: MatchTeamStatDtoExtractor,
    private val playerStatExtractor: MatchPlayerStatDtoExtractor,
    private val matchEntitySyncService: MatchEntitySyncService,
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
                matchEntitySyncService.syncMatchEntities(
                    fixtureApiId = fixtureApiId,
                    baseDto = baseDto,
                    lineupDto = lineupDto,
                    eventDto = eventDto,
                    teamStatDto = teamStatDto,
                    playerStatDto = playerStatDto,
                    playerContext = context,
                )

            log.info(
                "Match sync completed - Created: ${syncResult.createdCount}, Updated: ${syncResult.updatedCount}, Deleted: ${syncResult.deletedCount}",
            )

            // 경기 상태에 따른 상세 Result 반환
            return determineMatchDataSyncResult(dto, !lineupDto.isEmpty())
        } catch (e: Exception) {
            log.error("Failed to sync match data for fixture: {}", fixtureApiId, e)
            return MatchDataSyncResult.Error(
                message = "Match data sync failed: ${e.message}",
                kickoffTime = dto.fixture.date,
            )
        }
    }

    /**
     * 경기 상태를 분석하여 적절한 MatchDataSyncResult를 결정합니다.
     *
     * **상태 판단 기준:**
     * - **PreMatch** (NS, TBD, INT): 경기 전 단계
     * - **Live** (1H, HT, 2H, ET, BT, P, SUSP, LIVE): 경기 진행 중
     * - **PostMatch** (FT, AET, PEN): 경기 종료 후
     *
     * @param dto Match 데이터 DTO
     * @param hasLineup 라인업 데이터 존재 여부
     */
    private fun determineMatchDataSyncResult(
        dto: FullMatchSyncDto,
        hasLineup: Boolean,
    ): MatchDataSyncResult {
        val statusShort = dto.fixture.status.short
        val kickoffTime = dto.fixture.date
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
                val readyForLive = hasLineup && isKickoffImminent(kickoffTime)

                log.info(
                    "Pre-match phase - status={}, lineupCached={}, readyForLive={}",
                    statusShort,
                    hasLineup,
                    readyForLive,
                )

                MatchDataSyncResult.PreMatch(
                    lineupCached = hasLineup,
                    kickoffTime = kickoffTime,
                    readyForLive = readyForLive,
                )
            }
        }
    }

    /**
     * 경기 종료 상태인지 확인
     */
    private fun isMatchFinished(statusShort: String): Boolean =
        statusShort in FINISHED_STATUSES

    /**
     * 경기 진행 중 상태인지 확인
     */
    private fun isMatchLive(statusShort: String): Boolean =
        statusShort in LIVE_STATUSES

    /**
     * 킥오프가 임박했는지 확인 (킥오프 5분 전부터 라이브 Job으로 전환)
     */
    private fun isKickoffImminent(kickoffTime: OffsetDateTime?): Boolean {
        if (kickoffTime == null) return false
        val now = OffsetDateTime.now()
        val minutesUntilKickoff = Duration.between(now, kickoffTime).toMinutes()
        return minutesUntilKickoff <= KICKOFF_IMMINENT_THRESHOLD_MINUTES
    }

    /**
     * 경기 종료 후 경과 시간 계산 (분 단위)
     */
    private fun calculateMinutesSinceFinish(
        kickoffTime: OffsetDateTime?,
        elapsedMin: Int?,
    ): Long {
        if (kickoffTime == null || elapsedMin == null) {
            return 0L
        }

        val now = OffsetDateTime.now()
        val matchEndTime = kickoffTime.plusMinutes(elapsedMin.toLong())
        return Duration.between(matchEndTime, now).toMinutes().coerceAtLeast(0)
    }

    companion object {
        // 경기 종료 상태 코드
        private val FINISHED_STATUSES = setOf("FT", "AET", "PEN", "AWD", "WO", "CANC", "PST", "ABD")

        // 경기 진행 중 상태 코드
        private val LIVE_STATUSES = setOf("1H", "HT", "2H", "ET", "BT", "P", "SUSP", "LIVE")

        // 킥오프 임박 기준 (분)
        private const val KICKOFF_IMMINENT_THRESHOLD_MINUTES = 5L

        // 경기 종료 후 polling 중단 기준 (분)
        private const val POST_MATCH_POLLING_CUTOFF_MINUTES = 60L
    }
}
