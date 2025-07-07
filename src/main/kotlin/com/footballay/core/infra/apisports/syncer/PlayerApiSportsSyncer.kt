package com.footballay.core.infra.apisports.syncer

import com.footballay.core.infra.apisports.dto.PlayerApiSportsCreateDto
import com.footballay.core.infra.core.util.UidGenerator
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.TeamPlayerCore
import com.footballay.core.infra.persistence.core.repository.PlayerCoreRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.core.repository.TeamPlayerCoreRepository
import com.footballay.core.logger
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import kotlin.collections.get

@Component
class PlayerApiSportsSyncer (
    // Api sports repos
    private val playerApiSportsRepository: PlayerApiSportsRepository,
    private val teamApiSportsRepository: TeamApiSportsRepository,
    // Core repos
    private val playerCoreRepository: PlayerCoreRepository,
    private val teamPlayerCoreRepository: TeamPlayerCoreRepository,
    // etc
    private val uidGenerator: UidGenerator
) : ApiSportsNewPlayerSync {

    val log = logger()

    @Transactional
    override fun syncPlayers(players: List<PlayerSyncRequest>, teamApiId: Long?): List<PlayerCore> {
        if (teamApiId == null) {
            throw IllegalArgumentException("teamApiId is required for ApiSports implementation")
        }
        if (players.isEmpty()) {
            log.warn("At least one playerSyncRequest must be provided")
            return emptyList()
        }
        
        val dtos = players.map { convertToCreateDto(it) }
        syncPlayersOfTeam(teamApiId, dtos)
        val playerApiIds = players.mapNotNull { it.apiId }
        return playerApiSportsRepository.findAllByApiIdIn(playerApiIds).mapNotNull { it.playerCore }
    }

    @Transactional
    override fun syncPlayerWithoutTeam(players: List<PlayerSyncRequest>): List<PlayerCore> {
        if (players.isEmpty()) {
            log.warn("At least one player must be provided")
            return emptyList()
        }

        val dtos = players.map { convertToCreateDto(it) }

        val playerApiIds = dtos.mapNotNull { it.apiId }
        val existingPlayerApiSportsMap = findAndMapExistingPlayerApiSports(playerApiIds)
        
        log.info("syncPlayerWithoutTeam - Processing players: $playerApiIds")
        log.info("syncPlayerWithoutTeam - Existing PlayerApiSports found: ${existingPlayerApiSportsMap.keys}")

        // PlayerCore 들을 메모리에서 직접 수집 (N+1 방지)
        val collectedPlayerCores = mutableListOf<PlayerCore>()
        val processedPlayers = processPlayerApiSportsEntitiesWithCoreCollection(
            dtos, 
            existingPlayerApiSportsMap, 
            null,
            collectedPlayerCores
        )
        
        log.info("syncPlayerWithoutTeam - Returning ${collectedPlayerCores.size} PlayerCore entities from memory collection")
        return collectedPlayerCores
    }

    /**
     * 팀의 선수를 저장합니다. <br>
     * 이 메서드는 API Sports 에서 선수가 가질 수 있는 모든 필드를 고려한 DTO 를 사용합니다. <br>
     *
     * # 새로운 선수 저장
     * 새로운 선수 저장시 아래와 같은 고려사항에 유의해야 합니다. <br>
     * - API 엔티티 : (a) 이미 존재함 (b) 존재하지 않음
     * - CORE 엔티티 연관관계 : (a) 이미 존재함 (b) 존재하지 않음
     *
     * 1) API 엔티티 O , CORE 엔티티 O (연관관계) : API 엔티티 데이터 업데이트함
     * 2) API 엔티티 O , CORE 엔티티 X (연관관계) : CORE 엔티티 생성 후 연관관계 설정, API 엔티티 데이터 업데이트,
     * 3) API 엔티티 X , CORE 엔티티 x (연관관계) : CORE 엔티티 생성, API 엔티티 생성, CORE - API 연관관계 설정
     * 4) API 엔티티 X , CORE 엔티티 O (연관관계) : 이 case 는 조회되지 않습니다
     *
     * # 연관관계 검사
     * [PlayerCore] - [TeamPlayerCore] - [TeamCore] 다대다 연관관계를 검사하고 업데이트 합니다. <br>
     *
     * 1) dto 에 apiId 존재하는 선수 - Team 연관관계 없음 : 새롭게 연관관계를 추가합니다.
     * 2) dto 에 apiId 존재하는 선수 - Team 연관관계 있음 : 아무 작업도 하지 않습니다.
     * 3) dto 에 apiId 존재하지 않는 선수 - Team 연관관계 있음 : 아무 작업도 하지 않습니다.
     * 4) dto 에 apiId 존재하지 않는 선수 - Team 연관관계 없음 : 이 case 는 조회되지 않습니다.
     */
    @Transactional
    fun syncPlayersOfTeam(teamApiId: Long, dtos: List<PlayerApiSportsCreateDto>) {
        // TeamApiSports를 조회해서 연관관계를 통해 TeamCore를 가져옴
        val teamApiSports = teamApiSportsRepository.findTeamApiSportsByApiIdWithTeamCore(teamApiId)
            ?: throw IllegalArgumentException("Team not found with apiId=${teamApiId}")
        
        val team = teamApiSports.teamCore
            ?: throw IllegalArgumentException("TeamCore not found for team apiId=${teamApiId}")

        // 모든 DTO의 apiId 목록 (null 제거)
        val playerApiIds = dtos.mapNotNull { it.apiId }
        log.info("Processing players for teamApiId: $teamApiId, playerApiIds: $playerApiIds")

        // 기존 PlayerApiSports 엔티티 조회
        val existingPlayerApiSportsMap = findAndMapExistingPlayerApiSports(playerApiIds)
        log.info("Existing PlayerApiSports found: ${existingPlayerApiSportsMap.keys}")

        // PlayerApiSports 엔티티 처리 (기존 업데이트 또는 새로 생성)
        val processedPlayerApiSportsList = processPlayerApiSportsEntities(dtos, existingPlayerApiSportsMap, team)
        log.info("Processed PlayerApiSports entities: ${processedPlayerApiSportsList.map { it.apiId }}")

        // TeamCore와 PlayerCore 간의 연관관계 업데이트
        updateTeamPlayerRelationships(team, processedPlayerApiSportsList)
        log.info("Updated TeamCore with new PlayerCores for teamApiId: $teamApiId")
    }

    /**
     * PlayerSyncRequest를 PlayerApiSportsCreateDto로 변환
     */
    private fun convertToCreateDto(request: PlayerSyncRequest): PlayerApiSportsCreateDto {
        return PlayerApiSportsCreateDto(
            apiId = request.apiId,
            name = request.name,
            firstname = request.firstname,
            lastname = request.lastname,
            age = request.age,
            nationality = request.nationality,
            position = request.position,
            number = request.number,
            photo = request.photo
        )
    }

    /**
     * 기존 PlayerApiSports 엔티티들을 조회하고 apiId로 매핑하여 반환
     */
    private fun findAndMapExistingPlayerApiSports(playerApiIds: List<Long>): Map<Long, PlayerApiSports> {
        val existingPlayerApiSports = playerApiSportsRepository.findAllByApiIdIn(playerApiIds)
        return existingPlayerApiSports.associateBy { it.apiId ?: -1L }
    }

    /**
     * PlayerApiSports 엔티티 처리와 동시에 PlayerCore를 수집합니다. (N+1 방지용)
     */
    private fun processPlayerApiSportsEntitiesWithCoreCollection(
        playerDtos: List<PlayerApiSportsCreateDto>,
        existingPlayerApiSportsMap: Map<Long, PlayerApiSports>,
        team: TeamCore?,
        playerCoreCollector: MutableList<PlayerCore>
    ): List<PlayerApiSports> {
        if(existingPlayerApiSportsMap.isEmpty()) {
            log.warn("if existingPlayerApiSportsMap is empty, all players will be created as new")
        }

        val result = mutableListOf<PlayerApiSports>()

        playerDtos.forEach { dto ->
            val existingPlayerApiSports = existingPlayerApiSportsMap[dto.apiId]

            if (existingPlayerApiSports != null) {
                // 케이스 1: 기존 PlayerApiSports 업데이트
                val updatedPlayerApiSports = updateExistingPlayerApiSports(existingPlayerApiSports, dto)
                result.add(updatedPlayerApiSports)
                
                // PlayerCore 수집 (이미 메모리에 있음)
                updatedPlayerApiSports.playerCore?.let { playerCoreCollector.add(it) }
            } else {
                // 케이스 2: 새로운 PlayerApiSports 생성
                val newPlayerApiSports = createNewPlayerApiSports(dto, team)
                result.add(newPlayerApiSports)
                
                // PlayerCore 수집 (방금 생성된 것, 메모리에 있음)
                newPlayerApiSports.playerCore?.let { playerCoreCollector.add(it) }
            }
        }

        val savedEntities = playerApiSportsRepository.saveAll(result)
        log.info("Saved ${savedEntities.size} PlayerApiSports entities, collected ${playerCoreCollector.size} PlayerCore entities")
        
        return savedEntities
    }

    /**
     * PlayerApiSports 엔티티 처리 (기존 업데이트 또는 새로 생성) <br>
     * TeamCore가 null인 경우, 연관관계 설정 없이 PlayerApiSports만 생성합니다. <br>
     * 
     * ⚠️ 주의: 이 메서드는 호환성을 위해 유지되지만, PlayerCore 반환이 필요한 경우 
     * processPlayerApiSportsEntitiesWithCoreCollection 사용을 권장합니다.
     */
    private fun processPlayerApiSportsEntities(
        playerDtos: List<PlayerApiSportsCreateDto>,
        existingPlayerApiSportsMap: Map<Long, PlayerApiSports>,
        team: TeamCore?
    ): List<PlayerApiSports> {
        val dummyCollector = mutableListOf<PlayerCore>()
        return processPlayerApiSportsEntitiesWithCoreCollection(
            playerDtos, 
            existingPlayerApiSportsMap, 
            team, 
            dummyCollector
        )
    }

    /**
     * 기존 PlayerApiSports 엔티티 업데이트
     */
    private fun updateExistingPlayerApiSports(
        playerApiSports: PlayerApiSports,
        dto: PlayerApiSportsCreateDto
    ): PlayerApiSports {
        // 기본 정보 업데이트
        playerApiSports.apply {
            name = dto.name
            firstname = dto.firstname
            lastname = dto.lastname
            age = dto.age
            birthDate = dto.birthDate
            birthPlace = dto.birthPlace
            birthCountry = dto.birthCountry
            nationality = dto.nationality
            height = dto.height
            weight = dto.weight
            number = dto.number
            position = dto.position
            photo = dto.photo
        }

        // PlayerCore가 없는 비정상 케이스 처리
        if (playerApiSports.playerCore == null) {
            val newPlayerCore = createPlayerCore(dto)
            val savedPlayerCore = playerCoreRepository.save(newPlayerCore)
            playerApiSports.playerCore = savedPlayerCore
        }

        return playerApiSports
    }

    /**
     * 새로운 PlayerApiSports 엔티티 생성 <br>
     * TeamCore 가 null인 경우, 연관관계 설정 없이 PlayerApiSports만 생성합니다. <br>
     */
    private fun createNewPlayerApiSports(
        dto: PlayerApiSportsCreateDto,
        team: TeamCore?
    ): PlayerApiSports {
        // 1. PlayerCore 생성
        val playerCore = createPlayerCore(dto)
        val savedPlayerCore = playerCoreRepository.save(playerCore)

        // 2. TeamCore와 PlayerCore 연관관계 생성 (명시적으로 repository 사용)
        if(team != null) createTeamPlayerRelationship(team, savedPlayerCore)

        // 3. PlayerApiSports 생성 및 연관관계 설정
        return PlayerApiSports(
            playerCore = savedPlayerCore,
            apiId = dto.apiId,
            name = dto.name,
            firstname = dto.firstname,
            lastname = dto.lastname,
            age = dto.age,
            birthDate = dto.birthDate,
            birthPlace = dto.birthPlace,
            birthCountry = dto.birthCountry,
            nationality = dto.nationality,
            height = dto.height,
            weight = dto.weight,
            number = dto.number,
            position = dto.position,
            photo = dto.photo
        )
    }

    /**
     * PlayerCore 엔티티 생성
     */
    private fun createPlayerCore(dto: PlayerApiSportsCreateDto): PlayerCore {
        return PlayerCore(
            uid = uidGenerator.generateUid(),
            name = dto.name ?: "Unknown",
            firstname = dto.firstname,
            lastname = dto.lastname,
            age = dto.age,
            nationality = dto.nationality,
            position = dto.position,
            autoGenerated = true
        )
    }

    /**
     * TeamCore와 PlayerCore 간의 연관관계 생성 (명시적으로 repository 사용)
     */
    private fun createTeamPlayerRelationship(team: TeamCore, playerCore: PlayerCore) {
        val teamId = team.id ?: throw IllegalStateException("TeamCore must be saved before creating relationship")
        val playerId = playerCore.id ?: throw IllegalStateException("PlayerCore must be saved before creating relationship")
        
        // 이미 존재하는 관계인지 확인
        if (!teamPlayerCoreRepository.existsByTeamIdAndPlayerId(teamId, playerId)) {
            val teamPlayerCore = TeamPlayerCore(
                team = team,
                player = playerCore
            )
            teamPlayerCoreRepository.save(teamPlayerCore)
            log.info("Created team-player relationship: teamId=$teamId, playerId=$playerId")
        } else {
            log.info("Team-player relationship already exists: teamId=$teamId, playerId=$playerId")
        }
    }

    /**
     * TeamCore와 PlayerCore 간의 연관관계 업데이트 (명시적으로 repository 사용)
     */
    private fun updateTeamPlayerRelationships(
        team: TeamCore,
        processedPlayerApiSportsList: List<PlayerApiSports>,
    ) {
        val teamId = team.id ?: throw IllegalStateException("TeamCore must have an ID")
        
        // 1. 현재 팀에 연결된 모든 선수들 조회
        val currentTeamPlayers = teamPlayerCoreRepository.findByTeamId(teamId)
            .mapNotNull { it.player }
        
        // 2. 처리된 PlayerApiSports에서 PlayerCore 추출
        val processedPlayerCores = processedPlayerApiSportsList.mapNotNull { it.playerCore }

        // 3. 새로 추가되어야 할 연관관계 생성
        processedPlayerCores.forEach { playerCore ->
            val playerId = playerCore.id ?: return@forEach
            
            if (!teamPlayerCoreRepository.existsByTeamIdAndPlayerId(teamId, playerId)) {
                val teamPlayerCore = TeamPlayerCore(
                    team = team,
                    player = playerCore
                )
                teamPlayerCoreRepository.save(teamPlayerCore)
                log.info("Added player to team: teamId=$teamId, playerId=$playerId, playerName=${playerCore.name}")
            }
        }
    }
}