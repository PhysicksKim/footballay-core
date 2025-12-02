package com.footballay.core.infra.apisports.match

import com.footballay.core.infra.apisports.match.plan.base.MatchBaseDtoExtractor
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.event.MatchEventDtoExtractor
import com.footballay.core.infra.apisports.match.plan.lineup.MatchLineupDtoExtractor
import com.footballay.core.infra.apisports.match.plan.playerstat.MatchPlayerStatDtoExtractor
import com.footballay.core.infra.apisports.match.plan.teamstat.MatchTeamStatDtoExtractor
import com.footballay.core.infra.apisports.match.status.MatchStatusAnalyzer
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.logger
import org.springframework.stereotype.Service

/**
 * ApiSports Match 동기화 Facade
 *
 * FullMatchSyncDto를 받아 저장 계획(Plan)을 수립하고, MatchEntityPersistManager에 위임하여 실제 엔티티를 저장합니다.
 * MatchPlayerContext를 통해 선수 정보를 추출하고 관리하여, ApiSports의 불안정한 선수 ID 누락 문제를 완화합니다.
 */
@Service
class ApiSportsMatchEntitySyncFacadeImpl(
    private val baseDtoExtractor: MatchBaseDtoExtractor,
    private val lineupDtoExtractor: MatchLineupDtoExtractor,
    private val eventDtoExtractor: MatchEventDtoExtractor,
    private val teamStatExtractor: MatchTeamStatDtoExtractor,
    private val playerStatExtractor: MatchPlayerStatDtoExtractor,
    private val matchEntityPersistManager: MatchEntityPersistManager,
    private val matchStatusAnalyzer: MatchStatusAnalyzer,
) : ApiSportsMatchEntitySyncFacade {
    private val log = logger()

    override fun syncFixtureMatchEntities(dto: FullMatchSyncDto): MatchDataSyncResult {
        val fixtureApiId = dto.fixture.id
        log.info("[Match Sync] Start: fixtureApiId={}", fixtureApiId)

        return try {
            val context = MatchPlayerContext()
            val planDtos = extractPlanDtos(dto, context)

            log.debug(
                "[Match Sync] DTOs extracted: lineup={}, event={}, stat={}",
                context.lineupMpDtoMap.size, context.eventMpDtoMap.size, context.statMpDtoMap.size
            )

            val syncResult = matchEntityPersistManager.syncMatchEntities(
                fixtureApiId = fixtureApiId,
                baseDto = planDtos.base,
                lineupDto = planDtos.lineup,
                eventDto = planDtos.event,
                teamStatDto = planDtos.teamStat,
                playerStatDto = planDtos.playerStat,
                playerContext = context,
            )

            log.info(
                "[Match Sync] Complete: created={}, retained={}, deleted={}",
                syncResult.createdCount, syncResult.retainedCount, syncResult.deletedCount
            )

            matchStatusAnalyzer.analyzeAndDetermineResult(dto, planDtos.lineup)
        } catch (e: Exception) {
            log.error("[Match Sync] Failed: fixtureApiId={}", fixtureApiId, e)
            MatchDataSyncResult.Error(
                message = "Match data sync failed: ${e.message}",
                kickoffTime = dto.fixture.date?.toInstant(),
            )
        }
    }

    private fun extractPlanDtos(dto: FullMatchSyncDto, context: MatchPlayerContext) = PlanDtos(
        base = baseDtoExtractor.extractBaseMatch(dto),
        lineup = lineupDtoExtractor.extractLineup(dto, context),
        event = eventDtoExtractor.extractEvents(dto, context),
        teamStat = teamStatExtractor.extractTeamStats(dto),
        playerStat = playerStatExtractor.extractPlayerStats(dto, context),
    )

    private data class PlanDtos(
        val base: com.footballay.core.infra.apisports.match.plan.dto.FixtureApiSportsDto,
        val lineup: com.footballay.core.infra.apisports.match.plan.dto.MatchLineupPlanDto,
        val event: com.footballay.core.infra.apisports.match.plan.dto.MatchEventPlanDto,
        val teamStat: com.footballay.core.infra.apisports.match.plan.dto.MatchTeamStatPlanDto,
        val playerStat: com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerStatPlanDto,
    )
}
