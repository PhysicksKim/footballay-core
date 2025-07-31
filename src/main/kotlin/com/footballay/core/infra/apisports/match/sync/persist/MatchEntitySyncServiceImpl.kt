package com.footballay.core.infra.apisports.match.sync.persist

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.FixtureApiSportsDto
import com.footballay.core.infra.apisports.match.sync.dto.LineupSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.PlayerStatSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.TeamStatSyncDto
import com.footballay.core.infra.apisports.match.sync.loader.MatchDataLoader
import com.footballay.core.infra.apisports.match.sync.persist.base.BaseMatchEntitySyncer
import com.footballay.core.infra.apisports.match.sync.persist.event.manager.MatchEventManager
import com.footballay.core.infra.apisports.match.sync.persist.player.manager.MatchPlayerManager
import com.footballay.core.infra.apisports.syncer.match.persist.result.*
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * MatchEntitySyncService 구현체
 * 
 * **처리 단계:**
 * 1. 기존 엔티티 로드 (Fixture, MatchTeam, MatchPlayer, Event)
 * 2. Base DTO 처리 (Fixture + MatchTeam 생성/업데이트)
 * 3. MatchPlayer 처리 (MatchPlayerManager로 통합) + Lineup 정보 적용
 * 4. Event 처리 (생성/업데이트/삭제)
 * 5. PlayerStats 처리
 * 6. TeamStats 처리
 * 7. 데이터베이스 안전 반영
 * 
 * **현재 구현 상태:**
 * - Phase 1: 기존 엔티티 로드 ✅ 완료
 * - Phase 2: Base DTO 처리 ✅ 완료
 * - Phase 3: MatchPlayer 처리 + Lineup 정보 적용 ✅ 완료 (MatchPlayerManager로 통합)
 * - Phase 4: Event 처리 ✅ 완료 (MatchEventManager로 통합)
 * - Phase 5: PlayerStats 처리 ❌ TODO
 * - Phase 6: TeamStats 처리 ❌ TODO
 * - Phase 7: 실제 저장 ❌ TODO
 */
@Service
class MatchEntitySyncServiceImpl(
    private val matchDataLoader: MatchDataLoader,
    private val baseMatchEntitySyncer: BaseMatchEntitySyncer,
    private val matchPlayerManager: MatchPlayerManager,
    private val matchEventManager: MatchEventManager
) : MatchEntitySyncService {

    private val log = logger()

    @Transactional
    override fun syncMatchEntities(
        fixtureApiId: Long,
        baseDto: FixtureApiSportsDto,
        lineupDto: LineupSyncDto,
        eventDto: MatchEventSyncDto,
        teamStatDto: TeamStatSyncDto,
        playerStatDto: PlayerStatSyncDto,
        playerContext: MatchPlayerContext
    ): MatchEntitySyncResult {

        log.info("Starting entity sync for fixture: $fixtureApiId")

        try {
            // Phase 1: 기존 저장된 엔티티들 로드
            val entityBundle = MatchEntityBundle.Companion.createEmpty()
            matchDataLoader.loadContext(fixtureApiId, playerContext, entityBundle)
            log.info("Loaded existing entities - Players: ${entityBundle.allMatchPlayers.size}, Events: ${entityBundle.allEvents.size}")

            // Phase 2: Base DTO 처리 (Fixture + MatchTeam 생성/업데이트)
            val baseSyncResult = baseMatchEntitySyncer.syncBaseEntities(fixtureApiId, baseDto, entityBundle)
            if (!baseSyncResult.success) {
                log.error("Base entity sync failed: ${baseSyncResult.errorMessage}")
                return MatchEntitySyncResult.failure("Base entity sync failed: ${baseSyncResult.errorMessage}")
            }
            log.info("Base entities synced successfully - Home team: ${baseSyncResult.homeMatchTeam?.teamApiSports?.name}, Away team: ${baseSyncResult.awayMatchTeam?.teamApiSports?.name}")
            
            // Phase 3: MatchPlayer 처리 + Lineup 정보 적용 (MatchPlayerManager로 통합)
            val matchPlayerResult = matchPlayerManager.processMatchPlayers(playerContext, lineupDto, entityBundle)
            log.info("MatchPlayer processing completed - Total players: ${matchPlayerResult.totalPlayers}, Created: ${matchPlayerResult.createdCount}, Updated: ${matchPlayerResult.updatedCount}, Deleted: ${matchPlayerResult.deletedCount}")

            // Phase 4: Event 처리 (MatchEventManager로 통합)
            val matchEventResult = matchEventManager.processMatchEvents(eventDto, entityBundle)
            log.info("MatchEvent processing completed - Total events: ${matchEventResult.totalEvents}, Created: ${matchEventResult.createdCount}, Updated: ${matchEventResult.updatedCount}, Deleted: ${matchEventResult.deletedCount}")

            // Phase 5: PlayerStats 처리
            // TODO: PlayerStats 엔티티 생성/업데이트 로직 구현 필요
            
            // Phase 6: TeamStats 처리
            // TODO: TeamStats 엔티티 생성/업데이트 로직 구현 필요

            // Phase 7: 실제 저장 (TODO: MatchEntityPersister 구현 필요)
            // val persister = MatchEntityPersister(/* repositories */)
            // val persistResult = persister.persistChanges(playerChangeSet, eventChanges)

            // 임시 구현 - 실제 구현 전까지 기본값 반환
            log.info("Entity sync completed successfully for fixture: $fixtureApiId")

            return MatchEntitySyncResult.success(
                createdCount = matchPlayerResult.createdCount + matchEventResult.createdCount,
                updatedCount = matchPlayerResult.updatedCount + matchEventResult.updatedCount,
                deletedCount = matchPlayerResult.deletedCount + matchEventResult.deletedCount,
                playerChanges = MatchPlayerSyncResult(
                    created = matchPlayerResult.createdCount,
                    updated = matchPlayerResult.updatedCount,
                    deleted = matchPlayerResult.deletedCount
                ),
                eventChanges = MatchEventSyncResult(
                    created = matchEventResult.createdCount,
                    updated = matchEventResult.updatedCount,
                    deleted = matchEventResult.deletedCount
                )
            )

        } catch (e: Exception) {
            log.error("Failed to sync entities for fixture: $fixtureApiId", e)
            return MatchEntitySyncResult.failure("Entity sync failed: ${e.message}")
        }
    }
} 