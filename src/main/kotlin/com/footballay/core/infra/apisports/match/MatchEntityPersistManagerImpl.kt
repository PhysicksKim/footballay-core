package com.footballay.core.infra.apisports.match

import com.footballay.core.infra.apisports.match.persist.base.BaseMatchEntityManager
import com.footballay.core.infra.apisports.match.persist.event.manager.MatchEventManager
import com.footballay.core.infra.apisports.match.persist.event.manager.MatchEventProcessResult
import com.footballay.core.infra.apisports.match.persist.player.manager.MatchPlayerManager
import com.footballay.core.infra.apisports.match.persist.player.manager.MatchPlayerProcessResult
import com.footballay.core.infra.apisports.match.persist.playerstat.manager.PlayerStatsManager
import com.footballay.core.infra.apisports.match.persist.playerstat.result.PlayerStatsProcessResult
import com.footballay.core.infra.apisports.match.persist.teamstat.manager.TeamStatsManager
import com.footballay.core.infra.apisports.match.persist.teamstat.result.TeamStatsProcessResult
import com.footballay.core.infra.apisports.match.plan.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.dto.FixtureApiSportsDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchLineupPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerStatPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchTeamStatPlanDto
import com.footballay.core.infra.apisports.match.plan.loader.MatchDataLoader
import com.footballay.core.infra.apisports.syncer.match.persist.result.*
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Match 엔티티 저장 관리자
 *
 * Plan DTO들을 받아 실제 DB 엔티티로 변환하고 저장합니다.
 * MatchPlayer와 MatchTeam의 영속성 상태 유지 및 연관관계 설정이 핵심입니다.
 * (Core 엔티티와 Provider 엔티티 분리 설계를 준수)
 */
@Service
class MatchEntityPersistManagerImpl(
    private val matchDataLoader: MatchDataLoader,
    private val baseMatchEntityManager: BaseMatchEntityManager,
    private val matchPlayerManager: MatchPlayerManager,
    private val matchEventManager: MatchEventManager,
    private val playerStatsManager: PlayerStatsManager,
    private val teamStatsManager: TeamStatsManager,
) : MatchEntityPersistManager {
    private val log = logger()

    @Transactional
    override fun syncMatchEntities(
        fixtureApiId: Long,
        baseDto: FixtureApiSportsDto,
        lineupDto: MatchLineupPlanDto,
        eventDto: MatchEventPlanDto,
        teamStatDto: MatchTeamStatPlanDto,
        playerStatDto: MatchPlayerStatPlanDto,
        playerContext: MatchPlayerContext,
    ): MatchEntitySyncResult {
        log.info("[Entity Persist] Start: fixture={}", fixtureApiId)

        val entityBundle = loadExistingEntities(fixtureApiId, playerContext)
        syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        val matchPlayerResult = processMatchPlayers(playerContext, lineupDto, entityBundle)
        val matchEventResult = processNonCriticalStep("Event", MatchEventProcessResult(0, 0, 0, 0, emptyList())) {
            matchEventManager.processMatchEvents(eventDto, entityBundle)
        }
        val playerStatsResult = processNonCriticalStep("PlayerStats", PlayerStatsProcessResult(0, 0, 0, 0, emptyList())) {
            playerStatsManager.processPlayerStats(playerStatDto, entityBundle)
        }
        val teamStatsResult = processNonCriticalStep("TeamStats", TeamStatsProcessResult(false, false, 0, 0, null, null)) {
            teamStatsManager.processTeamStats(teamStatDto, entityBundle)
        }

        log.info("[Entity Persist] Complete: fixture={}", fixtureApiId)

        return buildSyncResult(matchPlayerResult, matchEventResult, playerStatsResult, teamStatsResult)
    }

    private fun loadExistingEntities(fixtureApiId: Long, playerContext: MatchPlayerContext): MatchEntityBundle {
        val bundle = MatchEntityBundle.createEmpty()
        runCatching {
            matchDataLoader.loadContext(fixtureApiId, playerContext, bundle)
            log.debug("[Load] Players={}, Events={}", bundle.allMatchPlayers.size, bundle.allEvents.size)
        }.onFailure { e ->
            log.error("[Load] Failed to load existing entities", e)
            throw e
        }
        return bundle
    }

    private fun syncBaseEntities(fixtureApiId: Long, baseDto: FixtureApiSportsDto, entityBundle: MatchEntityBundle) {
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)
        if (!result.success) {
            log.error("[Base] Sync failed: {}", result.errorMessage)
            throw IllegalStateException("Failed to sync base entities: ${result.errorMessage}")
        }
        log.debug("[Base] Synced: home={}, away={}", result.homeMatchTeam?.teamApiSports?.name, result.awayMatchTeam?.teamApiSports?.name)
    }

    private fun processMatchPlayers(
        playerContext: MatchPlayerContext,
        lineupDto: MatchLineupPlanDto,
        entityBundle: MatchEntityBundle
    ): MatchPlayerProcessResult {
        return runCatching {
            matchPlayerManager.processMatchTeamAndPlayers(playerContext, lineupDto, entityBundle).also {
                log.debug("[Player] Total={}, +{} -{}", it.totalPlayers, it.createdCount, it.deletedCount)
            }
        }.getOrElse { e ->
            log.error("[Player] Processing failed", e)
            throw IllegalStateException("Failed to process match players: ${e.message}", e)
        }
    }

    private inline fun <reified T> processNonCriticalStep(
        stepName: String,
        defaultValue: T,
        processor: () -> T
    ): T {
        return runCatching {
            processor().also { result ->
                when (result) {
                    is MatchEventProcessResult -> log.debug("[{}] Total={}, +{} -{}", stepName, result.totalEvents, result.createdCount, result.deletedCount)
                    is PlayerStatsProcessResult -> log.debug("[{}] Total={}, +{} -{}", stepName, result.totalStats, result.createdCount, result.deletedCount)
                    is TeamStatsProcessResult -> log.debug("[{}] Home={}, Away={}, +{}", stepName, result.hasHome, result.hasAway, result.createdCount)
                }
            }
        }.getOrElse { e ->
            log.warn("[{}] Processing failed, using default", stepName, e)
            defaultValue
        }
    }

    private fun buildSyncResult(
        matchPlayerResult: MatchPlayerProcessResult,
        matchEventResult: MatchEventProcessResult,
        playerStatsResult: PlayerStatsProcessResult,
        teamStatsResult: TeamStatsProcessResult,
    ): MatchEntitySyncResult {
        return MatchEntitySyncResult.success(
            createdCount = matchPlayerResult.createdCount + matchEventResult.createdCount +
                playerStatsResult.createdCount + teamStatsResult.createdCount,
            retainedCount = matchPlayerResult.retainedCount + matchEventResult.retainedCount +
                playerStatsResult.retainedCount + teamStatsResult.retainedCount,
            deletedCount = matchPlayerResult.deletedCount + matchEventResult.deletedCount + playerStatsResult.deletedCount,
            playerChanges = MatchPlayerSyncResult(
                created = matchPlayerResult.createdCount,
                retained = matchPlayerResult.retainedCount,
                deleted = matchPlayerResult.deletedCount,
            ),
            eventChanges = MatchEventSyncResult(
                created = matchEventResult.createdCount,
                retained = matchEventResult.retainedCount,
                deleted = matchEventResult.deletedCount,
            ),
        )
    }
}
