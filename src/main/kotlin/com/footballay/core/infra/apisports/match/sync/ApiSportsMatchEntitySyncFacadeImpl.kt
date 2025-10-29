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
        log.info("Starting match data sync for fixtureApiId=$fixtureApiId")

        try {
            val context = MatchPlayerContext()

            // Phase 1: DTO 추출
            val baseDto = baseDtoExtractor.extractBaseMatch(dto)
            val lineupDto = lineupDtoExtractor.extractLineup(dto, context)
            val eventDto = eventDtoExtractor.extractEvents(dto, context)
            val teamStatDto = teamStatExtractor.extractTeamStats(dto)
            val playerStatDto = playerStatExtractor.extractPlayerStats(dto, context)

            log.info(
                "Extracted DTOs - Lineup: ${context.lineupMpDtoMap.size}, Event: ${context.eventMpDtoMap.size}, Stat: ${context.statMpDtoMap.size}",
            )

            // Phase 2: 엔티티 동기화 (트랜잭션)
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

            return MatchDataSyncResult.ongoing(dto.fixture.date)
        } catch (e: Exception) {
            log.error("Failed to sync match data for fixture: $fixtureApiId", e)
            throw e
        }
    }
}
