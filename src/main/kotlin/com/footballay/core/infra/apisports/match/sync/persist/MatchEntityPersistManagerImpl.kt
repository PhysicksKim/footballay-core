package com.footballay.core.infra.apisports.match.sync.persist

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.FixtureApiSportsDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchLineupPlanDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventPlanDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerStatPlanDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchTeamStatPlanDto
import com.footballay.core.infra.apisports.match.sync.loader.MatchDataLoader
import com.footballay.core.infra.apisports.match.sync.persist.base.BaseMatchEntityManager
import com.footballay.core.infra.apisports.match.sync.persist.event.manager.MatchEventManager
import com.footballay.core.infra.apisports.match.sync.persist.event.manager.MatchEventProcessResult
import com.footballay.core.infra.apisports.match.sync.persist.player.manager.MatchPlayerManager
import com.footballay.core.infra.apisports.match.sync.persist.playerstat.manager.PlayerStatsManager
import com.footballay.core.infra.apisports.match.sync.persist.playerstat.result.PlayerStatsProcessResult
import com.footballay.core.infra.apisports.match.sync.persist.teamstat.manager.TeamStatsManager
import com.footballay.core.infra.apisports.match.sync.persist.teamstat.result.TeamStatsProcessResult
import com.footballay.core.infra.apisports.syncer.match.persist.result.*
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * MatchEntitySyncService 구현체
 * Match Data 관련 Dto 를 받아 DB Entity 로 저장합니다.
 * [MatchPlayerContext] 를 통해 Dto 들에 등장하는 MatchPlayer 들의 정보를 받습니다.
 *
 * ## id=null 선수 처리 중요성
 * ApiSports 는 불안정한 데이터 제공으로 인해 선수 아이디가 누락되는 경우가 빈번합니다.
 * 이 문제는 [MatchPlayerContext] 를 구성하는 쪽에서도 유의하여 동작하는 책임이며,
 * [MatchPlayerContext] 를 사용하는 측과 더불어 DB entity 로 변환하는 과정에서도 유의해야 합니다.
 * [com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer] 를 활용해 id=null 선수를 저장합니다.
 *
 * **처리 단계:**
 * 1. 기존 엔티티 로드 (Fixture, MatchTeam, MatchPlayer, Event)
 * 2. Base DTO 처리 (Fixture + MatchTeam 생성/업데이트)
 * 3. MatchPlayer 처리 (MatchPlayerManager로 통합) + Lineup 정보 적용
 * 4. Event 처리 (생성/업데이트/삭제)
 * 5. PlayerStats 처리 (PlayerStatsManager로 통합)
 * 6. TeamStats 처리 (TeamStatsManager로 통합)
 * 7. 데이터베이스 영속화 (각 Manager에서 완료)
 *
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
        log.info("Starting entity sync for fixture: {}", fixtureApiId)

        // 1. 기존 저장된 엔티티들 로드
        val entityBundle = MatchEntityBundle.createEmpty()
        try {
            matchDataLoader.loadContext(fixtureApiId, playerContext, entityBundle)
            log.info(
                "Loaded existing entities - Players: ${entityBundle.allMatchPlayers.size}, Events: ${entityBundle.allEvents.size}",
            )
        } catch (e: Exception) {
            log.error("Failed to load existing entities: {}", e.message, e)
            throw e
        }

        // 2. Base DTO 처리 (Fixture + MatchTeam 생성/업데이트)
        try {
            val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

            if (!result.success) {
                log.error("Base entity sync failed: {}", result.errorMessage)
                return MatchEntitySyncResult.failure(
                    "failed to sync base entities: ${result.errorMessage}",
                )
            }

            log.info(
                "Base entities synced - Home team: ${result.homeMatchTeam?.teamApiSports?.name}, Away team: ${result.awayMatchTeam?.teamApiSports?.name}",
            )
        } catch (e: Exception) {
            log.error("Failed to sync base entities: {}", e.message, e)
            return MatchEntitySyncResult.failure(
                "failed to sync base entities: ${e.message}",
            )
        }

        // 3. MatchPlayer 처리 + Lineup 정보 적용
        val matchPlayerResult =
            try {
                val result = matchPlayerManager.processMatchTeamAndPlayers(playerContext, lineupDto, entityBundle)
                log.info(
                    "MatchPlayer processing completed - Total: ${result.totalPlayers}, Created: ${result.createdCount}, Updated: ${result.updatedCount}, Deleted: ${result.deletedCount}",
                )
                result
            } catch (e: Exception) {
                log.error("Failed to process match players: {}", e.message, e)
                // 라인업 저장 에러시에는 이후 진행하기 어려우므로 실패 처리
                return MatchEntitySyncResult.failure(
                    "failed to process match lineup: ${e.message}",
                )
            }

        // 4. Event 처리
        val matchEventResult =
            try {
                val result = matchEventManager.processMatchEvents(eventDto, entityBundle)
                log.info(
                    "MatchEvent processing completed - Total: ${result.totalEvents}, Created: ${result.createdCount}, Updated: ${result.updatedCount}, Deleted: ${result.deletedCount}",
                )
                result
            } catch (e: Exception) {
                log.error("Failed to process match events: {}", e.message, e)
                MatchEventProcessResult(0, 0, 0, 0, emptyList())
            }

        // 5. PlayerStats 처리
        val playerStatsResult =
            try {
                val result = playerStatsManager.processPlayerStats(playerStatDto, entityBundle)
                log.info(
                    "PlayerStats processing completed - Total: ${result.totalStats}, Created: ${result.createdCount}, Updated: ${result.updatedCount}, Deleted: ${result.deletedCount}",
                )
                result
            } catch (e: Exception) {
                log.error("Failed to process player stats: {}", e.message, e)
                PlayerStatsProcessResult(0, 0, 0, 0, emptyList())
            }

        // 6. TeamStats 처리
        val teamStatsResult =
            try {
                val result = teamStatsManager.processTeamStats(teamStatDto, entityBundle)
                log.info(
                    "TeamStats processing completed - Home: ${result.hasHome}, Away: ${result.hasAway}, Created: ${result.createdCount}, Updated: ${result.updatedCount}",
                )
                result
            } catch (e: Exception) {
                log.error("Failed to process team stats: {}", e.message, e)
                TeamStatsProcessResult(false, false, 0, 0, null, null)
            }

        log.info("All entities persisted successfully for fixture: {}", fixtureApiId)

        return MatchEntitySyncResult.success(
            createdCount =
                matchPlayerResult.createdCount + matchEventResult.createdCount + playerStatsResult.createdCount +
                    teamStatsResult.createdCount,
            updatedCount =
                matchPlayerResult.updatedCount + matchEventResult.updatedCount + playerStatsResult.updatedCount +
                    teamStatsResult.updatedCount,
            deletedCount =
                matchPlayerResult.deletedCount + matchEventResult.deletedCount + playerStatsResult.deletedCount,
            playerChanges =
                MatchPlayerSyncResult(
                    created = matchPlayerResult.createdCount,
                    updated = matchPlayerResult.updatedCount,
                    deleted = matchPlayerResult.deletedCount,
                ),
            eventChanges =
                MatchEventSyncResult(
                    created = matchEventResult.createdCount,
                    updated = matchEventResult.updatedCount,
                    deleted = matchEventResult.deletedCount,
                ),
        )
    }
}
