package com.footballay.core.infra.apisports.syncer

import com.footballay.core.infra.apisports.dto.TeamApiSportsCreateDto
import com.footballay.core.infra.apisports.dto.VenueApiSportsCreateDto
import com.footballay.core.infra.core.util.UidGenerator
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.apisports.entity.VenueApiSports
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.VenueApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.LeagueTeamCore
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueTeamCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import com.footballay.core.logger
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class TeamApiSportsSyncer(
    // Api sports repos
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val teamApiRepository: TeamApiSportsRepository,
    private val venueRepository: VenueApiSportsRepository,
    // Core repos
    private val leagueCoreRepository: LeagueCoreRepository,
    private val teamCoreRepository: TeamCoreRepository,
    private val leagueTeamCoreRepository: LeagueTeamCoreRepository,
    // etc
    private val uidGenerator: UidGenerator
) {

    val log = logger()

    @Transactional
    fun saveTeamsOfLeague(leagueApiId: Long, teamDtos: List<TeamApiSportsCreateDto>) : List<TeamApiSports> {
        val leagueApiSports = leagueApiSportsRepository
            .findLeagueApiSportsByApiId(leagueApiId)
            ?: throw IllegalArgumentException("LeagueApiSports not found for apiId: $leagueApiId")
        val leagueCore = leagueApiSports.leagueCore
            ?: throw IllegalArgumentException("LeagueCore not found for LeagueApiSports with apiId: $leagueApiId")

        // 모든 DTO의 apiId 목록
        val teamApiIds = teamDtos.map { it.apiId }
        log.info("Processing teams for leagueApiId: $leagueApiId, teamApiIds: $teamApiIds")

        // 기존 TeamApiSports 엔티티 조회 (fetch join으로 TeamCore 함께 로드)
        val existingTeamApiSportsMap = findAndMapExistingTeamApiSports(teamApiIds)
        log.info("Existing TeamApiSports found: ${existingTeamApiSportsMap.keys}")

        // TeamApiSports 엔티티 처리 (기존 업데이트 또는 새로 생성)
        val processedTeamApiSportsList = processTeamApiSportsEntities(teamDtos, existingTeamApiSportsMap, leagueCore)
        log.info("Processed TeamApiSports entities: ${processedTeamApiSportsList.map { it.apiId }}")

        // LeagueCore와 TeamCore 간의 연관관계 업데이트
        updateLeagueTeamRelationships(leagueCore, processedTeamApiSportsList, teamApiIds)
        log.info("Updated LeagueCore with new TeamCores for leagueApiId: $leagueApiId")

        return processedTeamApiSportsList
    }

    @Transactional
    fun saveTeamWithLeagueId(leagueApiId: Long, teamDto: TeamApiSportsCreateDto) : TeamApiSports {
        val leagueApiSports = leagueApiSportsRepository
            .findLeagueApiSportsByApiId(leagueApiId)
            ?: throw IllegalArgumentException("LeagueApiSports not found for apiId: $leagueApiId")
        val leagueCore = leagueApiSports.leagueCore
            ?: throw IllegalArgumentException("LeagueCore not found for LeagueApiSports with apiId: $leagueApiId")

        // 기존 TeamApiSports 엔티티 조회 (fetch join으로 TeamCore 함께 로드)
        val existingTeam = findTeamApiSports(teamDto.apiId)
        val teamMap = if(existingTeam != null) { mapOf(existingTeam.apiId!! to existingTeam) } else { emptyMap() }

        // TeamApiSports 엔티티 처리 (기존 업데이트 또는 새로 생성)
        val processedTeamApiSportsList = processTeamApiSportsEntities(listOf(teamDto),teamMap,leagueCore)
        log.info("Processed TeamApiSports entities: ${processedTeamApiSportsList.map { it.apiId }}")

        // LeagueCore와 TeamCore 간의 연관관계 업데이트
        updateLeagueTeamRelationships(leagueCore, processedTeamApiSportsList, listOf(teamDto.apiId))
        log.info("Updated LeagueCore with new TeamCores for leagueApiId: $leagueApiId")

        return processedTeamApiSportsList.firstOrNull()
            ?: throw IllegalStateException("Failed to process TeamApiSports for apiId: ${teamDto.apiId}")
    }

    /**
     * 기존 TeamApiSports 엔티티들을 조회하고 apiId로 매핑하여 반환
     */
    private fun findAndMapExistingTeamApiSports(teamApiIds: List<Long>): Map<Long, TeamApiSports> {
        val existingTeamApiSports = teamApiRepository.findAllByApiIdIn(teamApiIds)
        return existingTeamApiSports.associateBy { it.apiId ?: -1L }
    }

    private fun findTeamApiSports(teamApiId: Long): TeamApiSports? {
        return teamApiRepository.findByApiId(teamApiId)
    }

    /**
     * TeamApiSports 기존 엔티티를 업데이트 하거나 새로 생성해 저장합니다.
     */
    private fun processTeamApiSportsEntities(
        teamDtos: List<TeamApiSportsCreateDto>,
        existingTeamApiSportsMap: Map<Long, TeamApiSports>,
        leagueCore: LeagueCore
    ): List<TeamApiSports> {
        val result = mutableListOf<TeamApiSports>()

        teamDtos.forEach { dto ->
            val existingTeamApiSports = existingTeamApiSportsMap[dto.apiId]

            if (existingTeamApiSports != null) {
                // 케이스 1: 기존 TeamApiSports 업데이트
                val updatedTeamApiSports = updateExistingTeamApiSports(existingTeamApiSports, dto)
                result.add(updatedTeamApiSports)
            } else {
                // 케이스 2: 새로운 TeamApiSports 생성
                val newTeamApiSports = createNewTeamApiSports(dto, leagueCore)
                result.add(newTeamApiSports)
            }
        }

        return teamApiRepository.saveAll(result)
    }

    /**
     * 기존 TeamApiSports 엔티티 업데이트
     */
    private fun updateExistingTeamApiSports(
        teamApiSports: TeamApiSports,
        dto: TeamApiSportsCreateDto
    ): TeamApiSports {
        // 기본 정보 업데이트
        teamApiSports.apply {
            name = dto.name
            code = dto.code
            country = dto.country
            founded = dto.founded
            national = dto.national
            logo = dto.logo
        }

        // TeamCore가 없는 비정상 케이스 처리
        if (teamApiSports.teamCore == null) {
            val newTeamCore = createTeamCore(dto)
            val savedTeamCore = teamCoreRepository.save(newTeamCore)
            teamApiSports.teamCore = savedTeamCore
        }

        // Venue 처리 (있으면 업데이트, 없으면 생성)
        if (dto.venue != null) {
            val venueEntity = processVenueEntity(dto.venue, teamApiSports.venue)
            teamApiSports.venue = venueEntity
        }

        return teamApiSports
    }

    /**
     * 새로운 TeamApiSports 엔티티 생성
     */
    private fun createNewTeamApiSports(
        dto: TeamApiSportsCreateDto,
        leagueCore: LeagueCore
    ): TeamApiSports {
        // 1. TeamCore 생성
        val teamCore = createTeamCore(dto)
        val savedTeamCore = teamCoreRepository.save(teamCore)

        // 2. LeagueCore와 TeamCore 연관관계 생성 (명시적으로 repository 사용)
        createLeagueTeamRelationship(leagueCore, savedTeamCore)

        // 3. Venue 생성 (있는 경우)
        val venueEntity = dto.venue?.let { processVenueEntity(it, null) }

        // 4. TeamApiSports 생성 및 연관관계 설정
        return TeamApiSports(
            teamCore = savedTeamCore,
            venue = venueEntity,
            apiId = dto.apiId,
            name = dto.name,
            code = dto.code,
            country = dto.country,
            founded = dto.founded,
            national = dto.national,
            logo = dto.logo
        )
    }

    /**
     * VenueApiSports 엔티티 처리 (업데이트 또는 생성)
     */
    private fun processVenueEntity(
        venueDto: VenueApiSportsCreateDto,
        existingVenue: VenueApiSports?
    ): VenueApiSports {
        if (existingVenue != null) {
            // 기존 Venue 업데이트
            existingVenue.apply {
                name = venueDto.name
                address = venueDto.address
                city = venueDto.city
                capacity = venueDto.capacity
                surface = venueDto.surface
                image = venueDto.image
            }
            return venueRepository.save(existingVenue)
        } else {
            // 새 Venue 생성
            val newVenue = VenueApiSports(
                apiId = venueDto.apiId,
                name = venueDto.name,
                address = venueDto.address,
                city = venueDto.city,
                capacity = venueDto.capacity,
                surface = venueDto.surface,
                image = venueDto.image
            )
            return venueRepository.save(newVenue)
        }
    }

    /**
     * TeamCore 엔티티 생성
     */
    private fun createTeamCore(dto: TeamApiSportsCreateDto): TeamCore {
        return TeamCore(
            uid = uidGenerator.generateUid(),
            name = dto.name,
            code = dto.code,
            country = dto.country,
            founded = dto.founded,
            national = dto.national ?: false,
            autoGenerated = true
        )
    }

    /**
     * LeagueCore와 TeamCore 간의 연관관계 생성 (명시적으로 repository 사용)
     */
    private fun createLeagueTeamRelationship(leagueCore: LeagueCore, teamCore: TeamCore) {
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

    /**
     * LeagueCore와 TeamCore 간의 연관관계 업데이트 (명시적으로 repository 사용)
     */
    private fun updateLeagueTeamRelationships(
        leagueCore: LeagueCore,
        processedTeamApiSportsList: List<TeamApiSports>,
        dtoTeamApiIds: List<Long>
    ) {
        val leagueId = leagueCore.id ?: throw IllegalStateException("LeagueCore must have an ID")
        
        // 1. 현재 리그에 연결된 모든 팀들 조회
        val currentLeagueTeams = leagueTeamCoreRepository.findByLeagueId(leagueId)
            .mapNotNull { it.team }
        
        // 2. 처리된 TeamApiSports에서 TeamCore 추출
        val processedTeamCores = processedTeamApiSportsList.mapNotNull { it.teamCore }

        // 3. 새로 추가되어야 할 연관관계 생성
        processedTeamCores.forEach { teamCore ->
            val teamId = teamCore.id ?: return@forEach
            
            if (!leagueTeamCoreRepository.existsByLeagueIdAndTeamId(leagueId, teamId)) {
                val leagueTeamCore = LeagueTeamCore(
                    league = leagueCore,
                    team = teamCore
                )
                leagueTeamCoreRepository.save(leagueTeamCore)
                log.info("Added team to league: leagueId=$leagueId, teamId=$teamId, teamName=${teamCore.name}")
            }
        }

        // 4. 제거되어야 할 연관관계 삭제 (강등 등의 이유)
        val processedTeamApiIds = processedTeamApiSportsList.mapNotNull { it.apiId }.toSet()
        
        // 현재 리그에 연결된 모든 TeamApiSports 조회
        val currentLeagueTeamApiSports = teamApiRepository.findAllByLeagueId(leagueId)
        
        // DTO에 없는 팀들의 연관관계 제거
        currentLeagueTeamApiSports.forEach { teamApiSports ->
            val apiId = teamApiSports.apiId
            if (apiId != null && !dtoTeamApiIds.contains(apiId)) {
                val teamCore = teamApiSports.teamCore
                if (teamCore != null) {
                    val teamId = teamCore.id ?: return@forEach
                    leagueTeamCoreRepository.deleteByLeagueIdAndTeamId(leagueId, teamId)
                    log.info("Removed team from league: leagueId=$leagueId, teamId=$teamId, teamName=${teamCore.name}, apiId=$apiId")
                }
            }
        }
    }
}