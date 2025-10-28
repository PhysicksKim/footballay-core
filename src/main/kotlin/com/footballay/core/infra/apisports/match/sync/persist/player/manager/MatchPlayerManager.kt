package com.footballay.core.infra.apisports.match.sync.persist.player.manager

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.apisports.match.sync.dto.LineupSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerDto
import com.footballay.core.infra.apisports.match.sync.persist.player.collector.MatchPlayerDtoCollector
import com.footballay.core.infra.apisports.match.sync.persist.player.planner.MatchPlayerChangePlanner
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.UniformColor
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchPlayerRepository
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.util.UidGenerator
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam

/**
 * PlayerContext 와 Lineup 기반으로 MatchPlayer 를 저장하며, Lineup 의 정보로 MatchTeam 에 정보를 추가합니다
 *
 * [MatchPlayerContext] 를 바탕으로 [ApiSportsMatchPlayer] 저장하고 Lineup을 바탕으로 MatchTeam과 연관관계를 설정합니다.
 * Lineup dto 를 활용해 [ApiSportsMatchTeam] 의 formation, color 도 함께 업데이트 합니다.
 */
@Component
class MatchPlayerManager(
    private val matchPlayerRepository: ApiSportsMatchPlayerRepository,
    private val playerApiSportsRepository: PlayerApiSportsRepository,
    private val uidGenerator: UidGenerator
) {

    private val log = logger()

    /**
     * MatchPlayer를 수집, 계획, 저장합니다.
     *
     * @param playerContext 선수 DTO 컨텍스트
     * @param lineupDto 라인업 정보
     * @param entityBundle 엔티티 번들 (업데이트됨)
     * @return 처리 결과
     */
    @Transactional
    fun processMatchTeamAndPlayers(
        playerContext: MatchPlayerContext,
        lineupDto: LineupSyncDto,
        entityBundle: MatchEntityBundle
    ): MatchPlayerProcessResult {
        log.info("Starting MatchPlayer processing with Lineup - Context players: ${playerContext.lineupMpDtoMap.size + playerContext.eventMpDtoMap.size + playerContext.statMpDtoMap.size}")
        
        try {
            val collectedDtos = MatchPlayerDtoCollector.collectFrom(playerContext)
            log.info("Collected ${collectedDtos.size} unique players from context")

            // 변경 계획 수립
            val entityKeyMap = MatchPlayerChangePlanner.entitiesToKeyMap(entityBundle.allMatchPlayers.values.toList())
            val playerChangeSet = MatchPlayerChangePlanner.planChanges(
                collectedDtos, 
                entityKeyMap, 
                uidGenerator,
                entityBundle.homeTeam,
                entityBundle.awayTeam
            )
            log.info("Planned changes - Create: ${playerChangeSet.createCount}, Update: ${playerChangeSet.updateCount}, Delete: ${playerChangeSet.deleteCount}")
            
            // Lineup 에 등장하는 선수는 lineup 관련 정보 추가
            val lineupEnhancedPlayers = enhancePlayersWithLineup(
                playerChangeSet.toCreate + playerChangeSet.toUpdate,
                lineupDto,
                collectedDtos,
                entityBundle
            )
            log.info("Enhanced MatchPlayers: ${lineupEnhancedPlayers.size}, Collected DTOs: ${collectedDtos.size}")

            // lineup 정보로 MatchTeam formation/color 업데이트
            updateMatchTeamsWithLineup(lineupDto, entityBundle)

            // PlayerApiSports 연결 및 영속 상태 저장
            val savedPlayers = persistChangesWithPlayerApiSports(lineupEnhancedPlayers, collectedDtos, entityBundle)
            log.info("Saved MatchPlayers: ${savedPlayers.size}, Create: ${playerChangeSet.createCount}, Update: ${playerChangeSet.updateCount}, Delete: ${playerChangeSet.deleteCount}")
            log.info("saved MatchPlayers name : ${savedPlayers.joinToString(separator = ", ") { "${it.name}_${it.id}_(${it.matchPlayerUid})" }}")

            // EntityBundle 에 MatchPlayer 업데이트
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
     * Lineup 정보를 MatchPlayer에 적용합니다.
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
     * MatchTeam의 formation과 color를 업데이트합니다.
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

    /** Color DTO를 엔티티로 변환 */
    private fun convertToUniformColor(color: LineupSyncDto.Color?): UniformColor? {
        return color?.let {
            UniformColor(
                primary = it.primary,
                number = it.number,
                border = it.border
            )
        }
    }

    /** 변경사항을 데이터베이스에 저장합니다. */
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

    /** PlayerApiSports를 배치 조회하여 연결합니다. (N+1 방지) */
    private fun connectPlayerApiSports(
        players: List<ApiSportsMatchPlayer>,
        collectedDtos: Map<String, MatchPlayerDto>
    ): List<ApiSportsMatchPlayer> {
        // 1. 필요한 apiId들을 수집
        val apiIds = collectedDtos.values
            .filter { dto -> dto.apiId != null }
            .map { it.apiId!! }
            .distinct()
        
        // 2. 일괄 조회로 PlayerApiSports 가져오기
        val playerApiSportsMap = if (apiIds.isNotEmpty()) {
            try {
                playerApiSportsRepository.findPlayerApiSportsByApiIdsWithPlayerCore(apiIds)
                    .associateBy { it.apiId ?: -1L }
            } catch (e: Exception) {
                log.warn("Failed to batch find PlayerApiSports for apiIds: $apiIds", e)
                emptyMap()
            }
        } else {
            emptyMap()
        }
        
        // 3. 각 플레이어에 PlayerApiSports 연결
        return players.map { player ->
            // DTO에서 apiId 찾기 (이름과 팀 ID로 매칭)
            val dto = collectedDtos.values.find { dto -> 
                dto.name == player.name && dto.teamApiId != null 
            }
            
            if (dto != null && player.playerApiSports == null && dto.apiId != null) {
                val playerApiSports = playerApiSportsMap[dto.apiId]
                player.playerApiSports = playerApiSports
                if (playerApiSports != null) {
                    log.debug("Connected PlayerApiSports for player: ${player.name} (apiId: ${dto.apiId})")
                } else {
                    log.debug("No PlayerApiSports found for player: ${player.name} (apiId: ${dto.apiId})")
                }
            }
            
            player
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