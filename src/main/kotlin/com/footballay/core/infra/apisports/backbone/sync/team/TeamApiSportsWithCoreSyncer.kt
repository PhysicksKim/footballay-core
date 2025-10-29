package com.footballay.core.infra.apisports.backbone.sync.team

import com.footballay.core.infra.apisports.shared.dto.TeamApiSportsCreateDto
import com.footballay.core.infra.core.LeagueTeamCoreSyncService
import com.footballay.core.infra.core.TeamCoreSyncService
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.apisports.entity.VenueApiSports
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.VenueApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.logger
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import kotlin.collections.get

/**
 * ApiSports 팀 데이터를 Core 시스템과 동기화하는 핵심 구현체
 *
 * ## 주요 책임
 * - ApiSports API에서 받은 팀 데이터를 Core 시스템과 연동
 * - TeamApiSports 엔티티와 TeamCore 엔티티 간의 연관관계 관리
 * - 리그별 팀 목록 동기화 및 연관관계 업데이트
 * - Core가 없는 팀 데이터에 대한 자동 Core 생성
 * - Venue(경기장) 정보와의 연관관계 관리
 *
 * ## 주요 진입 메서드
 * - `saveTeamsOfLeague()`: 특정 리그의 팀들을 동기화 (리그-팀 연관관계 포함)
 * - `saveTeamWithLeagueId()`: 단일 팀을 리그와 함께 동기화
 *
 * ## 핵심 동작 패턴
 * 1. **기존 데이터 분석**: TeamApiSports와 TeamCore 존재 여부에 따른 케이스 분리
 * 2. **Core 자동 생성**: Core가 없는 ApiSports 데이터에 대해 TeamCore 자동 생성
 * 3. **연관관계 관리**: LeagueCore와 TeamCore 간의 연관관계 동기화
 * 4. **Venue 처리**: 팀별 경기장 정보 동기화 및 연관관계 설정
 * 5. **배치 처리**: 성능 최적화를 위한 배치 저장 및 연관관계 처리
 *
 * ## 처리 케이스
 * - **Case 1**: TeamApiSports + TeamCore 모두 존재 → 업데이트
 * - **Case 2**: TeamApiSports만 존재, TeamCore 없음 → Core 생성 후 연결
 * - **Case 3**: 둘 다 없음 → 새로 생성
 *
 * ## 특별 처리
 * - **Venue 동기화**: 팀별 경기장 정보를 배치로 처리하여 성능 최적화
 * - **연관관계 제거**: DTO에 없는 팀들의 리그 연관관계 자동 제거
 *
 * @author Footballay Core Team
 * @since 1.0.0
 */
@Component
class TeamApiSportsWithCoreSyncer(
    // Api sports repos
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val teamApiRepository: TeamApiSportsRepository,
    private val venueRepository: VenueApiSportsRepository,
    // Core services
    private val teamCoreSyncService: TeamCoreSyncService,
    private val leagueTeamCoreSyncService: LeagueTeamCoreSyncService,
) : TeamApiSportsSyncer {
    private val log = logger()

    @Transactional
    override fun saveTeamsOfLeague(
        leagueApiId: Long,
        teamDtos: List<TeamApiSportsCreateDto>,
    ): List<TeamApiSports> {
        val leagueApiSports =
            leagueApiSportsRepository
                .findLeagueApiSportsByApiId(leagueApiId)
                ?: throw IllegalArgumentException("LeagueApiSports not found for apiId: $leagueApiId")
        val leagueCore =
            leagueApiSports.leagueCore
                ?: throw IllegalArgumentException("LeagueCore not found for LeagueApiSports with apiId: $leagueApiId")

        // 모든 DTO의 apiId 목록
        val teamApiIds = teamDtos.map { it.apiId }
        log.info("Processing teams for leagueApiId: $leagueApiId, teamApiIds: $teamApiIds")

        // 기존 TeamApiSports 엔티티 조회
        val existingTeamApiSportsMap = findAndMapExistingTeamApiSports(teamApiIds)
        log.info("Existing TeamApiSports found: ${existingTeamApiSportsMap.keys}")

        // TeamApiSports 엔티티 처리 (기존 업데이트 또는 새로 생성)
        val processedTeamApiSportsList =
            processTeamApiSportsEntitiesBatch(teamDtos, existingTeamApiSportsMap, leagueCore)
        log.info("Processed TeamApiSports entities: ${processedTeamApiSportsList.map { it.apiId }}")

        // LeagueCore와 TeamCore 간의 연관관계 업데이트
        val processedTeamCores = processedTeamApiSportsList.mapNotNull { it.teamCore }
        leagueTeamCoreSyncService.updateLeagueTeamRelationships(leagueCore, processedTeamCores, teamApiIds)
        log.info("Updated LeagueCore with new TeamCores for leagueApiId: $leagueApiId")

        return processedTeamApiSportsList
    }

    @Transactional
    override fun saveTeamWithLeagueId(
        leagueApiId: Long,
        teamDto: TeamApiSportsCreateDto,
    ): TeamApiSports {
        val leagueApiSports =
            leagueApiSportsRepository.findLeagueApiSportsByApiId(leagueApiId)
                ?: throw IllegalArgumentException("LeagueApiSports not found for apiId: $leagueApiId")
        val leagueCore =
            leagueApiSports.leagueCore
                ?: throw IllegalArgumentException("LeagueCore not found for LeagueApiSports with apiId: $leagueApiId")

        // 기존 TeamApiSports 엔티티 조회
        val existingTeam = findTeamApiSports(teamDto.apiId)
        val teamMap =
            if (existingTeam != null) {
                mapOf(existingTeam.apiId!! to existingTeam)
            } else {
                emptyMap()
            }

        // TeamApiSports 엔티티 처리 (기존 업데이트 또는 새로 생성)
        val processedTeamApiSportsList = processTeamApiSportsEntitiesBatch(listOf(teamDto), teamMap, leagueCore)
        log.info("Processed TeamApiSports entities: ${processedTeamApiSportsList.map { it.apiId }}")

        // LeagueCore와 TeamCore 간의 연관관계 업데이트
        val processedTeamCores = processedTeamApiSportsList.mapNotNull { it.teamCore }
        leagueTeamCoreSyncService.updateLeagueTeamRelationships(leagueCore, processedTeamCores, listOf(teamDto.apiId))
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

    private fun findTeamApiSports(teamApiId: Long): TeamApiSports? = teamApiRepository.findByApiId(teamApiId)

    /**
     * TeamApiSports 엔티티들을 배치로 처리하여 저장합니다.
     *
     * 처리 과정:
     * STEP 1: 케이스 분리 - 기존 데이터와 새로운 DTO를 비교하여 3가지 케이스로 분류
     * STEP 2: TeamCore 배치 생성 - Core가 필요한 케이스들에 대해 TeamCore를 배치로 생성
     * STEP 3: League-Team 연관관계 배치 생성 - 새로 생성된 TeamCore들과 LeagueCore 간의 연관관계 생성
     * STEP 4: TeamApiSports 엔티티 준비 - 모든 TeamApiSports 엔티티를 준비 (기존 업데이트 + 새로 생성)
     * STEP 5: Venue 배치 처리 - 모든 Venue 엔티티를 배치로 처리 (기존 업데이트 + 새로 생성)
     * STEP 6: 최종 배치 저장 - 모든 TeamApiSports 엔티티를 한 번에 저장
     *
     * @param teamDtos 처리할 팀 DTO 목록
     * @param existingTeamApiSportsMap 기존 TeamApiSports 엔티티 맵 (apiId -> TeamApiSports)
     * @param leagueCore 연관관계를 생성할 LeagueCore 엔티티
     * @return 처리된 TeamApiSports 엔티티 목록
     */
    private fun processTeamApiSportsEntitiesBatch(
        teamDtos: List<TeamApiSportsCreateDto>,
        existingTeamApiSportsMap: Map<Long, TeamApiSports>,
        leagueCore: LeagueCore,
    ): List<TeamApiSports> {
        // STEP 1: 케이스 분리
        val cases = separateCases(teamDtos, existingTeamApiSportsMap)

        // STEP 2: TeamCore 배치 생성
        val teamCoreMap = createTeamCoresBatch(cases)

        // STEP 3: League-Team 연관관계 배치 생성
        createLeagueTeamRelationshipsBatch(leagueCore, teamCoreMap)

        // STEP 4: TeamApiSports 엔티티 준비
        val allTeamApiSports = prepareAllTeamApiSports(cases, teamCoreMap)

        // STEP 5: Venue 배치 처리
        processVenuesBatch(allTeamApiSports, teamDtos)

        // STEP 5.5: 기존 TeamApiSports 엔티티 기본 정보 업데이트
        updateExistingTeamApiSports(allTeamApiSports, teamDtos)

        // STEP 6: 최종 배치 저장
        return saveAllTeamApiSports(allTeamApiSports)
    }

    /**
     * STEP 1: 케이스 분리
     *
     * 입력된 팀 DTO들과 기존 TeamApiSports 엔티티들을 비교하여 3가지 케이스로 분류합니다:
     * 1. bothExistTeams: TeamApiSports와 TeamCore가 모두 존재하는 경우 (업데이트 대상)
     * 2. apiOnlyTeams: TeamApiSports는 존재하지만 TeamCore가 없는 경우 (Core 연결 대상)
     * 3. bothNewDtos: TeamApiSports와 TeamCore가 모두 없는 경우 (새로 생성 대상)
     *
     * @param teamDtos 처리할 팀 DTO 목록
     * @param existingTeamApiSportsMap 기존 TeamApiSports 엔티티 맵
     * @return 분류된 케이스들
     */
    private fun separateCases(
        teamDtos: List<TeamApiSportsCreateDto>,
        existingTeamApiSportsMap: Map<Long, TeamApiSports>,
    ): ProcessingCases {
        val bothExistTeams = mutableListOf<TeamApiSports>()
        val apiOnlyTeams = mutableListOf<Pair<TeamApiSports, TeamApiSportsCreateDto>>()
        val bothNewDtos = mutableListOf<TeamApiSportsCreateDto>()

        teamDtos.forEach { dto ->
            val existingTeam = existingTeamApiSportsMap[dto.apiId]
            if (existingTeam != null) {
                if (existingTeam.teamCore != null) {
                    // CASE 1: 둘다 존재 (TeamApiSports + TeamCore) - 업데이트 대상
                    bothExistTeams.add(existingTeam)
                } else {
                    // CASE 2: API만 존재 (TeamApiSports만 있고 TeamCore 없음) - Core 연결 대상
                    apiOnlyTeams.add(existingTeam to dto)
                }
            } else {
                // CASE 3: 둘다 없음 (TeamApiSports + TeamCore 모두 없음) - 새로 생성 대상
                bothNewDtos.add(dto)
            }
        }

        return ProcessingCases(bothExistTeams, apiOnlyTeams, bothNewDtos)
    }

    /**
     * STEP 2: TeamCore 배치 생성
     *
     * Core가 필요한 케이스들(apiOnlyTeams, bothNewDtos)에 대해 TeamCore를 배치로 생성합니다.
     * 성능 최적화를 위해 개별 저장이 아닌 배치 저장을 사용합니다.
     *
     * @param cases 분류된 케이스들
     * @return 생성된 TeamCore 맵 (apiId -> TeamCore)
     */
    private fun createTeamCoresBatch(cases: ProcessingCases): Map<Long, TeamCore> {
        val teamCoreCreatePairs = mutableListOf<Pair<Long, TeamApiSportsCreateDto>>()

        // 새로 생성할 TeamCore들 (bothNewDtos)
        teamCoreCreatePairs.addAll(cases.bothNewDtos.map { it.apiId to it })

        // Core 연결이 필요한 TeamCore들 (apiOnlyTeams)
        teamCoreCreatePairs.addAll(cases.apiOnlyTeams.map { it.first.apiId!! to it.second })

        return if (teamCoreCreatePairs.isNotEmpty()) {
            teamCoreSyncService.createTeamCoresFromApiSports(teamCoreCreatePairs)
        } else {
            emptyMap()
        }
    }

    /**
     * STEP 3: League-Team 연관관계 배치 생성
     *
     * 새로 생성된 TeamCore들과 LeagueCore 간의 연관관계를 배치로 생성합니다.
     *
     * @param leagueCore 연관관계를 생성할 LeagueCore 엔티티
     * @param teamCoreMap 생성된 TeamCore 맵
     */
    private fun createLeagueTeamRelationshipsBatch(
        leagueCore: LeagueCore,
        teamCoreMap: Map<Long, TeamCore>,
    ) {
        if (teamCoreMap.values.isNotEmpty()) {
            leagueTeamCoreSyncService.createLeagueTeamRelationshipsBatch(leagueCore, teamCoreMap.values)
        }
    }

    /**
     * STEP 4: TeamApiSports 엔티티 준비
     *
     * 모든 TeamApiSports 엔티티를 준비합니다:
     * - 기존 엔티티들 (bothExistTeams, apiOnlyTeams)
     * - 새로 생성할 엔티티들 (bothNewDtos)
     *
     * @param cases 분류된 케이스들
     * @param teamCoreMap 생성된 TeamCore 맵
     * @return 모든 TeamApiSports 엔티티 맵 (apiId -> TeamApiSports)
     */
    private fun prepareAllTeamApiSports(
        cases: ProcessingCases,
        teamCoreMap: Map<Long, TeamCore>,
    ): Map<Long, TeamApiSports> {
        val allTeamApiSports = mutableMapOf<Long, TeamApiSports>()

        // 기존 엔티티들 추가
        cases.bothExistTeams.forEach { team ->
            team.apiId?.let { allTeamApiSports[it] = team }
        }

        // Core 연결이 필요한 엔티티들 처리
        cases.apiOnlyTeams.forEach { (teamApiSports, dto) ->
            val teamCore = teamCoreMap[teamApiSports.apiId]
            if (teamCore != null) {
                teamApiSports.teamCore = teamCore
            }
            teamApiSports.apiId?.let { allTeamApiSports[it] = teamApiSports }
        }

        // 새로 생성할 엔티티들 생성 (비영속 상태)
        cases.bothNewDtos.forEach { dto ->
            val teamCore = teamCoreMap[dto.apiId]
            if (teamCore != null) {
                val newTeamApiSports =
                    TeamApiSports(
                        teamCore = teamCore,
                        apiId = dto.apiId,
                        name = dto.name,
                        code = dto.code,
                        country = dto.country,
                        founded = dto.founded,
                        national = dto.national,
                        logo = dto.logo,
                    )
                allTeamApiSports[dto.apiId] = newTeamApiSports
            }
        }

        return allTeamApiSports
    }

    /**
     * STEP 5: Venue 배치 처리
     *
     * 모든 TeamApiSports 엔티티의 Venue를 배치로 처리합니다:
     * - 기존 Venue 업데이트 (preventUpdate가 false인 경우만)
     * - 새 Venue 생성
     * - Venue와 TeamApiSports 연결
     *
     * @param allTeamApiSports 모든 TeamApiSports 엔티티 맵
     * @param teamDtos 원본 팀 DTO 목록
     */
    private fun processVenuesBatch(
        allTeamApiSports: Map<Long, TeamApiSports>,
        teamDtos: List<TeamApiSportsCreateDto>,
    ) {
        val teamDtosMap = teamDtos.associateBy { it.apiId }
        val venuesToSave = mutableListOf<VenueApiSports>()
        val newVenues = mutableListOf<VenueApiSports>()

        // 모든 TeamApiSports 엔티티의 Venue 처리
        allTeamApiSports.values.forEach { teamApiSports ->
            val dto = teamDtosMap[teamApiSports.apiId]
            if (dto?.venue != null) {
                val venueDto = dto.venue
                if (teamApiSports.venue != null) {
                    // 기존 Venue 업데이트 (preventUpdate가 false인 경우만)
                    val venue = teamApiSports.venue!!
                    if (!venue.preventUpdate) {
                        venue.apply {
                            name = venueDto.name
                            address = venueDto.address
                            city = venueDto.city
                            capacity = venueDto.capacity
                            surface = venueDto.surface
                            image = venueDto.image
                        }
                        venuesToSave.add(venue)
                    }
                } else {
                    // 새 Venue 생성 (비영속 상태)
                    val newVenue =
                        VenueApiSports(
                            apiId = venueDto.apiId,
                            name = venueDto.name,
                            address = venueDto.address,
                            city = venueDto.city,
                            capacity = venueDto.capacity,
                            surface = venueDto.surface,
                            image = venueDto.image,
                        )
                    newVenues.add(newVenue)
                }
            }
        }

        // 모든 Venue를 한 번에 배치 저장 (기존 업데이트 + 새로 생성)
        val allVenuesToSave = venuesToSave + newVenues
        val persistedVenues =
            if (allVenuesToSave.isNotEmpty()) {
                venueRepository.saveAll(allVenuesToSave)
            } else {
                emptyList()
            }

        // Venue와 TeamApiSports 연결
        val venueToTeamIdsMap =
            teamDtos
                .filter { it.venue?.apiId != null }
                .groupBy { it.venue!!.apiId }
                .mapValues { (_, teams) -> teams.map { it.apiId } }

        persistedVenues.forEach { venue ->
            val venueApiId = venue.apiId
            if (venueApiId == null) return@forEach

            val teamApiIds = venueToTeamIdsMap[venueApiId]
            if (teamApiIds == null) return@forEach

            teamApiIds.forEach { teamApiId ->
                val teamApiEntity = allTeamApiSports[teamApiId]
                if (teamApiEntity != null) {
                    teamApiEntity.venue = venue
                }
            }
        }
    }

    /**
     * STEP 5.5: 기존 TeamApiSports 엔티티 기본 정보 업데이트
     *
     * 기존 TeamApiSports 엔티티들의 기본 정보를 DTO에서 업데이트합니다.
     *
     * @param allTeamApiSports 모든 TeamApiSports 엔티티 맵
     * @param teamDtos 원본 팀 DTO 목록
     */
    private fun updateExistingTeamApiSports(
        allTeamApiSports: Map<Long, TeamApiSports>,
        teamDtos: List<TeamApiSportsCreateDto>,
    ) {
        val teamDtosMap = teamDtos.associateBy { it.apiId }

        allTeamApiSports.values.forEach { teamApiSports ->
            val dto = teamDtosMap[teamApiSports.apiId]
            if (dto != null && !teamApiSports.preventUpdate) {
                teamApiSports.apply {
                    name = dto.name
                    code = dto.code
                    country = dto.country
                    founded = dto.founded
                    national = dto.national
                    logo = dto.logo
                }
            }
        }
    }

    /**
     * STEP 6: 최종 배치 저장
     *
     * 모든 TeamApiSports 엔티티를 한 번에 배치로 저장합니다.
     *
     * @param allTeamApiSports 모든 TeamApiSports 엔티티 맵
     * @return 저장된 TeamApiSports 엔티티 목록
     */
    private fun saveAllTeamApiSports(allTeamApiSports: Map<Long, TeamApiSports>): List<TeamApiSports> = teamApiRepository.saveAll(allTeamApiSports.values)
}
