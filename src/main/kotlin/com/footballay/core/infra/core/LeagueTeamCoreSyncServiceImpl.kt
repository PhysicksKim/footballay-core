package com.footballay.core.infra.core

import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueTeamCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.repository.LeagueTeamCoreRepository
import com.footballay.core.logger
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class LeagueTeamCoreSyncServiceImpl(
    private val leagueTeamCoreRepository: LeagueTeamCoreRepository,
    private val teamApiSportsRepository: TeamApiSportsRepository
) : LeagueTeamCoreSyncService {

    private val log = logger()

    @Transactional
    override fun createLeagueTeamRelationship(leagueCore: LeagueCore, teamCore: TeamCore) {
        val leagueId = leagueCore.id ?: throw IllegalStateException("LeagueCore must be saved before creating relationship")
        val teamId = teamCore.id ?: throw IllegalStateException("TeamCore must be saved before creating relationship")

        // 이미 존재하는 관계인지 확인
        if (!leagueTeamCoreRepository.existsByLeagueIdAndTeamId(leagueId, teamId)) {
            val leagueTeamCore = LeagueTeamCore(
                league = leagueCore,
                team = teamCore
            )
            leagueTeamCoreRepository.save(leagueTeamCore)
            log.info("Created league-team relationship: leagueId=$leagueId, teamId=$teamId")
        } else {
            log.info("League-team relationship already exists: leagueId=$leagueId, teamId=$teamId")
        }
    }

    @Transactional
    override fun updateLeagueTeamRelationships(
        leagueCore: LeagueCore,
        teamCores: List<TeamCore>,
        teamApiIds: List<Long>
    ) {
        val leagueId = leagueCore.id ?: throw IllegalStateException("LeagueCore must have an ID")

        // 배치 처리를 위한 데이터 준비
        val teamIds = teamCores.mapNotNull { it.id }
        val processedTeamApiIds = teamApiIds.toSet()

        // 1. 현재 리그에 연결된 모든 연관관계를 한 번에 조회
        val existingRelationships = leagueTeamCoreRepository.findByLeagueId(leagueId)
        val existingTeamIds = existingRelationships.mapNotNull { it.team?.id }.toSet()

        // 2. 새로 추가되어야 할 연관관계를 배치로 생성
        val newRelationships = mutableListOf<LeagueTeamCore>()
        teamIds.forEach { teamId ->
            if (!existingTeamIds.contains(teamId)) {
                val teamCore = teamCores.find { it.id == teamId }
                if (teamCore != null) {
                    val leagueTeamCore = LeagueTeamCore(
                        league = leagueCore,
                        team = teamCore
                    )
                    newRelationships.add(leagueTeamCore)
                }
            }
        }

        // 배치로 새 연관관계 저장
        if (newRelationships.isNotEmpty()) {
            leagueTeamCoreRepository.saveAll(newRelationships)
            log.info("Added ${newRelationships.size} teams to league: leagueId=$leagueId, teamIds=${newRelationships.map { it.team?.id }}")
        }

        // 3. 제거되어야 할 연관관계를 배치로 삭제
        val currentLeagueTeamApiSports = teamApiSportsRepository.findAllByLeagueId(leagueId)
        val relationshipsToRemove = mutableListOf<LeagueTeamCore>()

        currentLeagueTeamApiSports.forEach { teamApiSports ->
            val apiId = teamApiSports.apiId
            if (apiId != null && !processedTeamApiIds.contains(apiId)) {
                val teamCore = teamApiSports.teamCore
                if (teamCore != null) {
                    val teamId = teamCore.id ?: return@forEach
                    val relationship = existingRelationships.find { it.team?.id == teamId }
                    if (relationship != null) {
                        relationshipsToRemove.add(relationship)
                    }
                }
            }
        }

        // 배치로 연관관계 삭제
        if (relationshipsToRemove.isNotEmpty()) {
            leagueTeamCoreRepository.deleteAll(relationshipsToRemove)
            log.info("Removed ${relationshipsToRemove.size} teams from league: leagueId=$leagueId, teamIds=${relationshipsToRemove.map { it.team?.id }}")
        }
    }

    @Transactional
    override fun removeTeamFromLeague(leagueId: Long, teamId: Long) {
        leagueTeamCoreRepository.deleteByLeagueIdAndTeamId(leagueId, teamId)
        log.info("Removed team from league: leagueId=$leagueId, teamId=$teamId")
    }

    /**
     * 여러 TeamCore와 LeagueCore 간의 연관관계를 배치로 생성
     */
    @Transactional
    override fun createLeagueTeamRelationshipsBatch(leagueCore: LeagueCore, teamCores: Collection<TeamCore>) {
        val leagueId = leagueCore.id ?: throw IllegalStateException("LeagueCore must have an ID")
        val teamIds = teamCores.mapNotNull { it.id }

        if (teamIds.isEmpty()) {
            log.info("No teams to create relationships for league: leagueId=$leagueId")
            return
        }

        // 기존 연관관계 조회 (한 번에)
        val existingRelationships = leagueTeamCoreRepository.findByLeagueId(leagueId)
        val existingTeamIds = existingRelationships.mapNotNull { it.team?.id }.toSet()

        // 새로 생성할 연관관계 수집
        val newRelationships = mutableListOf<LeagueTeamCore>()
        teamIds.forEach { teamId ->
            if (!existingTeamIds.contains(teamId)) {
                val teamCore = teamCores.find { it.id == teamId }
                if (teamCore != null) {
                    val leagueTeamCore = LeagueTeamCore(
                        league = leagueCore,
                        team = teamCore
                    )
                    newRelationships.add(leagueTeamCore)
                }
            }
        }

        // 배치로 새 연관관계 저장
        if (newRelationships.isNotEmpty()) {
            leagueTeamCoreRepository.saveAll(newRelationships)
            log.info("Batch created ${newRelationships.size} league-team relationships: leagueId=$leagueId, teamIds=${newRelationships.map { it.team?.id }}")
        } else {
            log.info("All league-team relationships already exist: leagueId=$leagueId")
        }
    }
} 