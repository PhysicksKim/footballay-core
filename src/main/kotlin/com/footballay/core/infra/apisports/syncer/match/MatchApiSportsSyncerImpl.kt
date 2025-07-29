package com.footballay.core.infra.apisports.syncer.match

import com.footballay.core.infra.apisports.live.ActionAfterMatchSync
import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.syncer.match.base.MatchBaseDtoExtractor
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.infra.apisports.syncer.match.event.MatchEventDtoExtractor
import com.footballay.core.infra.apisports.syncer.match.lineup.MatchLineupDtoExtractor
import com.footballay.core.infra.apisports.syncer.match.playerstat.MatchPlayerStatDtoExtractor
import com.footballay.core.infra.apisports.syncer.match.teamstat.MatchTeamStatDtoExtractor
import com.footballay.core.infra.apisports.syncer.match.persist.MatchEntitySyncService
import com.footballay.core.logger
import org.springframework.stereotype.Service

/**
 * ApiSports 라이브 매치 데이터를 엔티티별 DTO로 분해하는 서비스
 * 
 * `FullMatchSyncDto`를 받아서 각 엔티티별로 필요한 DTO를 추출하고,
 * MatchPlayer 사전 추출을 통해 엔티티 저장 로직을 단순화합니다.
 * 
 * **핵심 책임:**
 * - `FullMatchSyncDto`를 엔티티별 DTO로 분해
 * - MatchPlayer 사전 추출 및 Context 관리
 * - 엔티티 저장 로직과 분리하여 트랜잭션 최적화
 * 
 * **처리 과정:**
 * 1. **Base DTO 추출**: Fixture, MatchTeam 기본 정보
 * 2. **Lineup DTO 추출**: 라인업 정보 및 MatchPlayer 추출
 * 3. **Event DTO 추출**: 이벤트 정보 및 관련 MatchPlayer 추출. 이때 subst in/out player/assist 정규화 처리
 * 4. **Stats DTO 추출**: 팀/선수 통계 정보
 * 5. **MatchEntitySyncService**로 실제 엔티티 저장 위임
 * 
 * **MatchPlayer 사전 추출의 중요성:**
 * - Event, PlayerStats에서 복잡한 MatchPlayer 처리 로직 단순화
 * - 중복 선수 처리 및 Orphan 엔티티 방지
 * - 트랜잭션 범위 최적화
 * 
 * **아키텍처상 역할:**
 * - DTO 추출과 엔티티 저장의 중간 계층
 * - Provider별 동기화와 엔티티 저장 로직의 분리점
 * 
 * @see MatchEntitySyncService
 * @see MatchPlayerContext
 * 
 * AI가 작성한 주석
 */
@Service
class MatchApiSportsSyncerImpl(
    private val baseMatchSync: MatchBaseDtoExtractor,
    private val lineupSync: MatchLineupDtoExtractor,
    private val eventSync: MatchEventDtoExtractor,
    private val teamStatSync: MatchTeamStatDtoExtractor,
    private val playerStatSync: MatchPlayerStatDtoExtractor,
    private val matchEntitySyncService: MatchEntitySyncService
) : MatchApiSportsSyncer {

    private val log = logger()

    override fun syncFixtureMatchEntities(dto: FullMatchSyncDto): ActionAfterMatchSync {
        val fixtureApiId = dto.fixture.id
        log.info("Starting sync match data for fixtureApiId=$fixtureApiId")

        try {
            // Phase 1: DTO 추출 (트랜잭션 불필요)
            val context = MatchPlayerContext()

            val baseDto = baseMatchSync.extractBaseMatch(dto)  // TODO: extractBaseMatch로 메서드명 변경
            val lineupDto = lineupSync.extractLineup(dto, context)  // TODO: extractLineup으로 메서드명 변경
            val eventDto = eventSync.extractEvents(dto, context)  // TODO: extractEvents로 메서드명 변경
            val teamStatDto = teamStatSync.extractTeamStats(dto)  // TODO: extractTeamStats로 메서드명 변경
            val playerStatDto = playerStatSync.extractPlayerStats(dto, context)  // TODO: extractPlayerStats로 메서드명 변경

            log.info("Extracted DTOs - Lineup players: ${context.lineupMpDtoMap.size}, Event players: ${context.eventMpDtoMap.size}, Stat players: ${context.statMpDtoMap.size}")

            // Phase 2: 실제 엔티티 동기화 (트랜잭션 시작)
            val syncResult = matchEntitySyncService.syncMatchEntities(
                fixtureApiId = fixtureApiId,
                baseDto = baseDto,
                lineupDto = lineupDto,
                eventDto = eventDto,
                teamStatDto = teamStatDto,
                playerStatDto = playerStatDto,
                playerContext = context
            )

            log.info("Match sync completed - Created: ${syncResult.createdCount}, Updated: ${syncResult.updatedCount}, Deleted: ${syncResult.deletedCount}")

            return ActionAfterMatchSync.ongoing(
                dto.fixture.date
            )
        } catch (e: Exception) {
            log.error("Failed to sync full match data for fixture: $fixtureApiId", e)
            throw e
        }
    }
}