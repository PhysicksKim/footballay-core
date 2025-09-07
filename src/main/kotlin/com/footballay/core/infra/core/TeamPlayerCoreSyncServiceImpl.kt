package com.footballay.core.infra.core

import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.TeamPlayerCore
import com.footballay.core.infra.persistence.core.repository.TeamPlayerCoreRepository
import com.footballay.core.logger
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TeamPlayerCoreSyncServiceImpl(
    private val teamPlayerCoreRepository: TeamPlayerCoreRepository,
    private val playerApiSportsRepository: PlayerApiSportsRepository
) : TeamPlayerCoreSyncService {

    private val log = logger()

    @Transactional
    override fun createTeamPlayerRelationship(teamCore: TeamCore, playerCore: PlayerCore) {
        val teamId = teamCore.id ?: throw IllegalStateException("TeamCore must be saved before creating relationship")
        val playerId = playerCore.id ?: throw IllegalStateException("PlayerCore must be saved before creating relationship")

        // 이미 존재하는 관계인지 확인
        if (!teamPlayerCoreRepository.existsByTeamIdAndPlayerId(teamId, playerId)) {
            val teamPlayerCore = TeamPlayerCore(
                team = teamCore,
                player = playerCore
            )
            teamPlayerCoreRepository.save(teamPlayerCore)
            log.info("Created team-player relationship: teamId=$teamId, playerId=$playerId")
        } else {
            log.info("Team-player relationship already exists: teamId=$teamId, playerId=$playerId")
        }
    }

    @Transactional
    override fun createTeamPlayerRelationshipsBatch(teamCore: TeamCore, playerCores: List<PlayerCore>) {
        val teamId = teamCore.id ?: throw IllegalStateException("TeamCore must have an ID")
        val playerIds = playerCores.mapNotNull { it.id }

        if (playerIds.isEmpty()) {
            log.info("No players to create relationships for team: teamId=$teamId")
            return
        }

        val existingRelationships = teamPlayerCoreRepository.findByTeamId(teamId)
        val existingPlayerIds = existingRelationships.mapNotNull { it.player?.id }.toSet()

        val newRelationships = mutableListOf<TeamPlayerCore>()
        playerIds.forEach { playerId ->
            if (!existingPlayerIds.contains(playerId)) {
                val playerCore = playerCores.find { it.id == playerId }
                if (playerCore != null) {
                    val teamPlayerCore = TeamPlayerCore(team = teamCore, player = playerCore)
                    newRelationships.add(teamPlayerCore)
                }
            }
        }

        if (newRelationships.isNotEmpty()) {
            teamPlayerCoreRepository.saveAll(newRelationships)
            log.info("Batch created ${newRelationships.size} team-player relationships: teamId=$teamId, playerIds=${newRelationships.map { it.player?.id }}")
        } else {
            log.info("All team-player relationships already exist: teamId=$teamId")
        }
    }

    @Transactional
    override fun updateTeamPlayerRelationships(teamCore: TeamCore, playerCores: List<PlayerCore>, playerApiIds: List<Long>) {
        val teamId = teamCore.id ?: throw IllegalStateException("TeamCore must have an ID")

        // 배치 처리를 위한 데이터 준비
        val playerIds = playerCores.mapNotNull { it.id }
        val processedPlayerApiIds = playerApiIds.toSet()

        // 1. 현재 팀에 연결된 모든 연관관계를 한 번에 조회
        val existingRelationships = teamPlayerCoreRepository.findByTeamId(teamId)
        val existingPlayerIds = existingRelationships.mapNotNull { it.player?.id }.toSet()

        // 2. 새로 추가되어야 할 연관관계를 배치로 생성
        val newRelationships = mutableListOf<TeamPlayerCore>()
        playerIds.forEach { playerId ->
            if (!existingPlayerIds.contains(playerId)) {
                val playerCore = playerCores.find { it.id == playerId }
                if (playerCore != null) {
                    val teamPlayerCore = TeamPlayerCore(
                        team = teamCore,
                        player = playerCore
                    )
                    newRelationships.add(teamPlayerCore)
                }
            }
        }

        // 배치로 새 연관관계 저장
        if (newRelationships.isNotEmpty()) {
            teamPlayerCoreRepository.saveAll(newRelationships)
            log.info("Added ${newRelationships.size} players to team: teamId=$teamId, playerIds=${newRelationships.map { it.player?.id }}")
        }

        // 3. 제거되어야 할 연관관계를 배치로 삭제
        // 현재 팀에 속한 모든 PlayerCore의 apiId를 조회
        val currentTeamPlayerApiIds = existingRelationships
            .mapNotNull { it.player?.id }
            .mapNotNull { playerId ->
                playerApiSportsRepository.findByPlayerCoreId(playerId)?.apiId
            }
            .toSet()

        val relationshipsToRemove = mutableListOf<TeamPlayerCore>()

        currentTeamPlayerApiIds.forEach { apiId ->
            if (!processedPlayerApiIds.contains(apiId)) {
                val playerCore = existingRelationships
                    .find { it.player?.id?.let { playerId -> 
                        playerApiSportsRepository.findByPlayerCoreId(playerId)?.apiId == apiId 
                    } == true }
                    ?.player
                
                if (playerCore != null) {
                    val relationship = existingRelationships.find { it.player?.id == playerCore.id }
                    if (relationship != null) {
                        relationshipsToRemove.add(relationship)
                    }
                }
            }
        }

        // 배치로 연관관계 삭제
        if (relationshipsToRemove.isNotEmpty()) {
            teamPlayerCoreRepository.deleteAll(relationshipsToRemove)
            log.info("Removed ${relationshipsToRemove.size} players from team: teamId=$teamId, playerIds=${relationshipsToRemove.map { it.player?.id }}")
        }
    }

    @Transactional
    override fun removePlayerFromTeam(teamId: Long, playerId: Long) {
        val relationships = teamPlayerCoreRepository.findByTeamIdAndPlayerId(teamId, playerId)
        if (relationships.isNotEmpty()) {
            teamPlayerCoreRepository.deleteAll(relationships)
            log.info("Removed player from team: teamId=$teamId, playerId=$playerId")
        } else {
            log.info("Team-player relationship not found for deletion: teamId=$teamId, playerId=$playerId")
        }
    }
} 