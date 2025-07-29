package com.footballay.core.infra.apisports.syncer.match.persist.player.manager

import com.footballay.core.infra.apisports.syncer.match.context.MatchEntityBundle
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.apisports.syncer.match.dto.MatchPlayerDto
import com.footballay.core.infra.apisports.syncer.match.dto.LineupSyncDto
import com.footballay.core.infra.apisports.syncer.match.persist.player.collector.MatchPlayerDtoCollector
import com.footballay.core.infra.apisports.syncer.match.persist.player.planner.MatchPlayerChangePlanner
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.UniformColor
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchPlayerRepository
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.util.UidGenerator
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * MatchPlayer 통합 관리자
 * 
 * MatchPlayer의 수집, 계획, Lineup 정보 적용, 저장을 통합하여 관리합니다.
 * 
 * **처리 과정:**
 * 1. MatchPlayerDtoCollector로 우선순위 기반 수집
 * 2. MatchPlayerChangePlanner로 변경 계획 수립
 * 3. Lineup 정보로 MatchPlayer 완전 구성
 * 4. MatchTeam formation/color 업데이트
 * 5. PlayerApiSports 연결 및 영속 상태 저장
 * 6. EntityBundle 업데이트
 * 
 * **특징:**
 * - 영속 상태 MatchPlayer를 EntityBundle에 반영
 * - Event에서 참조할 수 있는 안전한 상태 보장
 * - 단일 책임으로 MatchEntitySyncServiceImpl 단순화
 * - PlayerApiSports 연결 로직 포함
 * - Lineup 정보와 함께 완전한 MatchPlayer 처리
 */
@Component
class MatchPlayerManager(
    private val matchPlayerRepository: ApiSportsMatchPlayerRepository,
    private val playerApiSportsRepository: PlayerApiSportsRepository,
    private val uidGenerator: UidGenerator
) {

    private val log = logger()

    /**
     * MatchPlayer를 Lineup 정보와 함께 수집, 계획, 저장하여 영속 상태로 만듭니다.
     * 
     * @param playerContext MatchPlayer DTO들이 담긴 컨텍스트
     * @param lineupDto Lineup 정보 DTO
     * @param entityBundle 기존 엔티티 번들 (업데이트됨)
     * @return MatchPlayer 처리 결과
     */
    @Transactional
    fun processMatchPlayers(
        playerContext: MatchPlayerContext,
        lineupDto: LineupSyncDto,
        entityBundle: MatchEntityBundle
    ): MatchPlayerProcessResult {
        log.info("Starting MatchPlayer processing with Lineup - Context players: ${playerContext.lineupMpDtoMap.size + playerContext.eventMpDtoMap.size + playerContext.statMpDtoMap.size}")
        
        try {
            // 1단계: 우선순위 기반 수집
            val collectedDtos = MatchPlayerDtoCollector.collectFrom(playerContext)
            log.info("Collected ${collectedDtos.size} unique players from context")
            
            // 2단계: 변경 계획 수립
            val entityKeyMap = MatchPlayerChangePlanner.entitiesToKeyMap(entityBundle.allMatchPlayers.values.toList())
            val playerChangeSet = MatchPlayerChangePlanner.planChanges(
                collectedDtos, 
                entityKeyMap, 
                uidGenerator,
                entityBundle.homeTeam,
                entityBundle.awayTeam
            )
            log.info("Planned changes - Create: ${playerChangeSet.createCount}, Update: ${playerChangeSet.updateCount}, Delete: ${playerChangeSet.deleteCount}")
            
            // 3단계: Lineup 정보로 MatchPlayer 완전 구성
            val lineupEnhancedPlayers = enhancePlayersWithLineup(
                playerChangeSet.toCreate + playerChangeSet.toUpdate,
                lineupDto,
                collectedDtos,
                entityBundle
            )
            
            // 4단계: MatchTeam formation/color 업데이트
            updateMatchTeamsWithLineup(lineupDto, entityBundle)
            
            // 5단계: PlayerApiSports 연결 및 영속 상태 저장
            val savedPlayers = persistChangesWithPlayerApiSports(lineupEnhancedPlayers, collectedDtos, entityBundle)
            
            // 6단계: EntityBundle 업데이트 (Map으로 변환)
            val savedPlayersMap = savedPlayers.associate { player ->
                val key = MatchPlayerKeyGenerator.generateMatchPlayerKey(player.playerApiSports?.apiId, player.name)
                key to player
            }
            entityBundle.allMatchPlayers = savedPlayersMap

            log.info("MatchPlayer processing completed - Total saved: ${savedPlayers.size}")
            return MatchPlayerProcessResult(
                totalPlayers = savedPlayers.size,
                createdCount = playerChangeSet.createCount,
                updatedCount = playerChangeSet.updateCount,
                deletedCount = playerChangeSet.deleteCount,
                savedPlayers = savedPlayers
            )
        } catch (e: Exception) {
            log.error("Failed to process MatchPlayers", e)
            throw e
        }
    }

    /**
     * Lineup 정보로 MatchPlayer를 완전 구성합니다.
     * 
     * @param players 생성/업데이트할 MatchPlayer 목록
     * @param lineupDto Lineup 정보
     * @param collectedDtos 수집된 DTO 맵
     * @param entityBundle 엔티티 번들
     * @return Lineup 정보가 적용된 MatchPlayer 목록
     */
    private fun enhancePlayersWithLineup(
        players: List<ApiSportsMatchPlayer>,
        lineupDto: LineupSyncDto,
        collectedDtos: Map<String, MatchPlayerDto>,
        entityBundle: MatchEntityBundle
    ): List<ApiSportsMatchPlayer> {
        if (lineupDto.isEmpty()) {
            log.info("Lineup is empty, skipping lineup enhancement")
            return players
        }
        
        log.info("Enhancing ${players.size} players with lineup information")
        
        return players.map { player ->
            // DTO에서 해당 선수 찾기
            val dto = collectedDtos.values.find { dto -> 
                dto.name == player.name && dto.teamApiId != null 
            }
            
            if (dto != null) {
                // 팀별 Lineup 정보 찾기
                val teamLineup = when (dto.teamApiId) {
                    entityBundle.homeTeam?.getTeamApiId() -> lineupDto.home
                    entityBundle.awayTeam?.getTeamApiId() -> lineupDto.away
                    else -> null
                }
                
                if (teamLineup != null) {
                    // 선발/후보 구분
                    val playerKey = MatchPlayerKeyGenerator.generateMatchPlayerKey(dto.apiId, dto.name)
                    val isStarter = teamLineup.startMpKeys.contains(playerKey)
                    val isSubstitute = teamLineup.subMpKeys.contains(playerKey)
                    
                    // Lineup 정보 적용
                    player.substitute = !isStarter
                    
                    // DTO에서 추가 정보 가져오기
                    dto.position?.let { player.position = it }
                    dto.grid?.let { player.grid = it }
                    dto.number?.let { player.number = it }
                    
                    log.debug("Enhanced player: ${player.name} (position: ${player.position}, substitute: ${player.substitute})")
                }
            }
            
            player
        }
    }

    /**
     * MatchTeam의 formation과 color를 Lineup 정보로 업데이트합니다.
     * 
     * @param lineupDto Lineup 정보
     * @param entityBundle 엔티티 번들
     */
    private fun updateMatchTeamsWithLineup(
        lineupDto: LineupSyncDto,
        entityBundle: MatchEntityBundle
    ) {
        if (lineupDto.isEmpty()) {
            log.info("Lineup is empty, skipping MatchTeam updates")
            return
        }
        
        log.info("Updating MatchTeams with lineup information")
        
        // Home Team 업데이트
        lineupDto.home?.let { homeLineup ->
            entityBundle.homeTeam?.let { homeTeam ->
                homeTeam.formation = homeLineup.formation
                homeTeam.playerColor = convertToUniformColor(homeLineup.playerColor)
                homeTeam.goalkeeperColor = convertToUniformColor(homeLineup.goalkeeperColor)
                log.info("Updated home team formation: ${homeLineup.formation}")
            }
        }
        
        // Away Team 업데이트
        lineupDto.away?.let { awayLineup ->
            entityBundle.awayTeam?.let { awayTeam ->
                awayTeam.formation = awayLineup.formation
                awayTeam.playerColor = convertToUniformColor(awayLineup.playerColor)
                awayTeam.goalkeeperColor = convertToUniformColor(awayLineup.goalkeeperColor)
                log.info("Updated away team formation: ${awayLineup.formation}")
            }
        }
    }

    /**
     * LineupSyncDto.Color를 UniformColor로 변환합니다.
     */
    private fun convertToUniformColor(color: LineupSyncDto.Color?): UniformColor? {
        return color?.let {
            UniformColor(
                primary = it.primary,
                number = it.number,
                border = it.border
            )
        }
    }

    /**
     * PlayerApiSports 연결과 함께 변경 계획을 실제 데이터베이스에 반영합니다.
     */
    private fun persistChangesWithPlayerApiSports(
        players: List<ApiSportsMatchPlayer>,
        collectedDtos: Map<String, MatchPlayerDto>,
        entityBundle: MatchEntityBundle
    ): List<ApiSportsMatchPlayer> {
        val allPlayers = mutableListOf<ApiSportsMatchPlayer>()
        
        // 1. 삭제 처리 (기존 로직 유지)
        val toDelete = entityBundle.allMatchPlayers.values.filter { existingPlayer ->
            !players.any { it.matchPlayerUid == existingPlayer.matchPlayerUid }
        }
        if (toDelete.isNotEmpty()) {
            matchPlayerRepository.deleteAll(toDelete)
            log.info("Deleted ${toDelete.size} MatchPlayers")
        }
        
        // 2. 생성 및 업데이트 처리 (PlayerApiSports 연결 포함)
        if (players.isNotEmpty()) {
            // PlayerApiSports 연결
            val connectedPlayers = connectPlayerApiSports(players, collectedDtos)
            
            val savedPlayers = matchPlayerRepository.saveAll(connectedPlayers)
            allPlayers.addAll(savedPlayers)
            log.info("Saved ${savedPlayers.size} MatchPlayers")
        }
        
        return allPlayers
    }

    /**
     * MatchPlayer에 PlayerApiSports를 연결합니다.
     */
    private fun connectPlayerApiSports(
        players: List<ApiSportsMatchPlayer>,
        collectedDtos: Map<String, MatchPlayerDto>
    ): List<ApiSportsMatchPlayer> {
        return players.map { player ->
            // DTO에서 apiId 찾기 (이름과 팀 ID로 매칭)
            val dto = collectedDtos.values.find { dto -> 
                dto.name == player.name && dto.teamApiId != null 
            }
            
            if (dto != null && player.playerApiSports == null) {
                val playerApiSports = findPlayerApiSports(dto.apiId) // teamApiId가 아닌 apiId 사용
                player.playerApiSports = playerApiSports
                log.debug("Connected PlayerApiSports for player: ${player.name} (apiId: ${dto.apiId})")
            }
            
            player
        }
    }

    /**
     * PlayerApiSports 연결 로직
     */
    private fun findPlayerApiSports(apiId: Long?): PlayerApiSports? {
        return apiId?.let { 
            try {
                playerApiSportsRepository.findPlayerApiSportsByApiIdWithPlayerCore(it)
            } catch (e: Exception) {
                log.warn("Failed to find PlayerApiSports for apiId: $apiId", e)
                null
            }
        }
    }
}

/**
 * MatchPlayer 처리 결과
 */
data class MatchPlayerProcessResult(
    val totalPlayers: Int,
    val createdCount: Int,
    val updatedCount: Int,
    val deletedCount: Int,
    val savedPlayers: List<ApiSportsMatchPlayer>
) {
    companion object {
        fun empty(): MatchPlayerProcessResult {
            return MatchPlayerProcessResult(0, 0, 0, 0, emptyList())
        }
    }
} 