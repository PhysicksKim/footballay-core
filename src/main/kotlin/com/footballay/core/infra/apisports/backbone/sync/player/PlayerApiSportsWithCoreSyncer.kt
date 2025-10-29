package com.footballay.core.infra.apisports.backbone.sync.player

import com.footballay.core.infra.apisports.backbone.sync.PlayerApiSportsCreateDto
import com.footballay.core.infra.core.PlayerCoreSyncService
import com.footballay.core.infra.core.TeamPlayerCoreSyncService
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.logger
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

/**
 * ApiSports 선수 데이터를 Core 시스템과 동기화하는 핵심 구현체
 *
 * ## 주요 책임
 * - ApiSports API에서 받은 선수 데이터를 Core 시스템과 연동
 * - PlayerApiSports 엔티티와 PlayerCore 엔티티 간의 연관관계 관리
 * - 팀별 선수 목록 동기화 및 연관관계 업데이트
 * - Core가 없는 선수 데이터에 대한 자동 Core 생성
 *
 * ## 주요 진입 메서드
 * - `syncPlayersOfTeam()`: 특정 팀의 선수들을 동기화 (팀-선수 연관관계 포함)
 * - `syncPlayerWithoutTeam()`: 팀 없이 선수만 동기화 (Core 생성만)
 *
 * ## 핵심 동작 패턴
 * 1. **기존 데이터 분석**: PlayerApiSports와 PlayerCore 존재 여부에 따른 케이스 분리
 * 2. **Core 자동 생성**: Core가 없는 ApiSports 데이터에 대해 PlayerCore 자동 생성
 * 3. **연관관계 관리**: TeamCore와 PlayerCore 간의 연관관계 동기화
 * 4. **배치 처리**: 성능 최적화를 위한 배치 저장 및 연관관계 처리
 * 5. **데이터 무결성**: preventUpdate 플래그를 통한 선택적 업데이트 제어
 *
 * ## 처리 케이스
 * - **Case 1**: PlayerApiSports + PlayerCore 모두 존재 → 업데이트
 * - **Case 2**: PlayerApiSports만 존재, PlayerCore 없음 → Core 생성 후 연결
 * - **Case 3**: 둘 다 없음 → 새로 생성
 *
 * @author Footballay Core Team
 * @since 1.0.0
 */
@Component
class PlayerApiSportsWithCoreSyncer(
    // Api sports repos
    private val teamApiSportsRepository: TeamApiSportsRepository,
    private val playerApiRepository: PlayerApiSportsRepository,
    // Core services
    private val playerCoreSyncService: PlayerCoreSyncService,
    private val teamPlayerCoreSyncService: TeamPlayerCoreSyncService,
) : PlayerApiSportsSyncer {
    private val log = logger()

    @Transactional
    override fun syncPlayersOfTeam(
        teamApiId: Long,
        dtos: List<PlayerApiSportsCreateDto>,
    ): List<PlayerApiSports> {
        val teamApiSports =
            teamApiSportsRepository.findTeamApiSportsByApiIdWithTeamCore(teamApiId)
                ?: throw IllegalArgumentException("Team not found with apiId=$teamApiId")

        val teamCore =
            teamApiSports.teamCore
                ?: throw IllegalArgumentException("TeamCore not found for team apiId=$teamApiId")

        // 모든 DTO의 apiId 목록
        val playerApiIds = dtos.mapNotNull { it.apiId }
        log.info("Processing players for teamApiId: $teamApiId, playerApiIds: $playerApiIds")

        // 기존 PlayerApiSports 엔티티 조회
        val existingPlayerApiSportsMap = findAndMapExistingPlayerApiSports(playerApiIds)
        log.info("Existing PlayerApiSports found: ${existingPlayerApiSportsMap.keys}")

        // PlayerApiSports 엔티티 처리 (기존 업데이트 또는 새로 생성)
        val processedPlayerApiSportsList =
            processPlayerApiSportsEntitiesBatch(dtos, existingPlayerApiSportsMap, teamCore)
        log.info("Processed PlayerApiSports entities: ${processedPlayerApiSportsList.map { it.apiId }}")

        // TeamCore와 PlayerCore 간의 연관관계 업데이트
        val processedPlayerCores = processedPlayerApiSportsList.mapNotNull { it.playerCore }
        teamPlayerCoreSyncService.updateTeamPlayerRelationships(teamCore, processedPlayerCores, playerApiIds)
        log.info("Updated TeamCore with new PlayerCores for teamApiId: $teamApiId")

        return processedPlayerApiSportsList
    }

    @Transactional
    override fun syncPlayerWithoutTeam(dtos: List<PlayerApiSportsCreateDto>): List<PlayerCore> {
        if (dtos.isEmpty()) {
            log.warn("At least one player must be provided")
            return emptyList()
        }

        val playerApiIds = dtos.mapNotNull { it.apiId }
        val existingPlayerApiSportsMap = findAndMapExistingPlayerApiSports(playerApiIds)

        log.info("syncPlayerWithoutTeam - Processing players: $playerApiIds")
        log.info("syncPlayerWithoutTeam - Existing PlayerApiSports found: ${existingPlayerApiSportsMap.keys}")

        // PlayerCore 들을 메모리에서 직접 수집 (N+1 방지)
        val collectedPlayerCores = mutableListOf<PlayerCore>()
        val processedPlayers =
            processPlayerApiSportsEntitiesWithCoreCollection(
                dtos,
                existingPlayerApiSportsMap,
                null,
                collectedPlayerCores,
            )

        log.info(
            "syncPlayerWithoutTeam - Returning ${collectedPlayerCores.size} PlayerCore entities from memory collection",
        )
        return collectedPlayerCores
    }

    /**
     * 기존 PlayerApiSports 엔티티들을 조회하고 apiId로 매핑하여 반환
     */
    private fun findAndMapExistingPlayerApiSports(playerApiIds: List<Long>): Map<Long, PlayerApiSports> {
        val existingPlayerApiSports = playerApiRepository.findAllByApiIdIn(playerApiIds)
        return existingPlayerApiSports.associateBy { it.apiId ?: -1L }
    }

    /**
     * PlayerApiSports 엔티티들을 배치로 처리하여 저장합니다.
     *
     * ### 처리 과정
     * - STEP 1: 케이스 분리 - 기존 데이터와 새로운 DTO를 비교하여 3가지 케이스로 분류
     * - STEP 2: PlayerCore 배치 생성 - Core가 필요한 케이스들에 대해 PlayerCore를 배치로 생성
     * - STEP 3: Core Team-Player 연관관계 배치 생성 - 새로 생성된 PlayerCore들과 TeamCore 간의 연관관계 생성
     * - STEP 4: PlayerApiSports 엔티티 준비 - 모든 PlayerApiSports 엔티티를 준비 (기존 업데이트 + 새로 생성)
     * - STEP 5: 기존 PlayerApiSports 엔티티 기본 정보 업데이트
     * - STEP 6: 최종 배치 저장 - 모든 PlayerApiSports 엔티티를 한 번에 저장
     *
     * @param playerDtos 처리할 선수 DTO 목록
     * @param existingPlayerApiSportsMap 기존 PlayerApiSports 엔티티 맵 (apiId -> PlayerApiSports)
     * @param teamCore 연관관계를 생성할 TeamCore 엔티티
     * @return 처리된 PlayerApiSports 엔티티 목록
     */
    private fun processPlayerApiSportsEntitiesBatch(
        playerDtos: List<PlayerApiSportsCreateDto>,
        existingPlayerApiSportsMap: Map<Long, PlayerApiSports>,
        teamCore: TeamCore,
    ): List<PlayerApiSports> {
        // STEP 1: 케이스 분리
        val cases = separateCases(playerDtos, existingPlayerApiSportsMap)

        // STEP 2: PlayerCore 배치 생성
        val playerCoreMap = createPlayerCoresBatch(cases)

        // STEP 3: Core Team-Player 연관관계 배치 생성
        createTeamPlayerRelationshipsBatch(teamCore, playerCoreMap)

        // STEP 4: PlayerApiSports 엔티티 준비
        val allPlayerApiSports = prepareAllPlayerApiSports(cases, playerCoreMap)

        // STEP 5: 기존 PlayerApiSports 엔티티 기본 정보 업데이트
        updateExistingPlayerApiSports(allPlayerApiSports, playerDtos)

        // STEP 6: 최종 배치 저장
        return saveAllPlayerApiSports(allPlayerApiSports)
    }

    /**
     * STEP 1: 케이스 분리
     *
     * 입력된 선수 DTO들과 기존 PlayerApiSports 엔티티들을 비교하여 3가지 케이스로 분류합니다:
     * 1. bothExistPlayers: PlayerApiSports와 PlayerCore가 모두 존재하는 경우 (업데이트 대상)
     * 2. apiOnlyPlayers: PlayerApiSports는 존재하지만 PlayerCore가 없는 경우 (Core 연결 대상)
     * 3. bothNewDtos: PlayerApiSports와 PlayerCore가 모두 없는 경우 (새로 생성 대상)
     *
     * @param playerDtos 처리할 선수 DTO 목록
     * @param existingPlayerApiSportsMap 기존 PlayerApiSports 엔티티 맵
     * @return 분류된 케이스들
     */
    private fun separateCases(
        playerDtos: List<PlayerApiSportsCreateDto>,
        existingPlayerApiSportsMap: Map<Long, PlayerApiSports>,
    ): PlayerProcessingCases {
        val bothExistPlayers = mutableListOf<PlayerApiSports>()
        val apiOnlyPlayers = mutableListOf<Pair<PlayerApiSports, PlayerApiSportsCreateDto>>()
        val bothNewDtos = mutableListOf<PlayerApiSportsCreateDto>()

        playerDtos.forEach { dto ->
            val existingPlayer = existingPlayerApiSportsMap[dto.apiId]
            if (existingPlayer != null) {
                if (existingPlayer.playerCore != null) {
                    // CASE 1: 둘다 존재 (PlayerApiSports + PlayerCore) - 업데이트 대상
                    bothExistPlayers.add(existingPlayer)
                } else {
                    // CASE 2: API만 존재 (PlayerApiSports만 있고 PlayerCore 없음) - Core 연결 대상
                    apiOnlyPlayers.add(existingPlayer to dto)
                }
            } else {
                // CASE 3: 둘다 없음 (PlayerApiSports + PlayerCore 모두 없음) - 새로 생성 대상
                bothNewDtos.add(dto)
            }
        }

        return PlayerProcessingCases(bothExistPlayers, apiOnlyPlayers, bothNewDtos)
    }

    /**
     * STEP 2: PlayerCore 배치 생성
     *
     * Core가 필요한 케이스들(apiOnlyPlayers, bothNewDtos)에 대해 PlayerCore를 배치로 생성합니다.
     * 성능 최적화를 위해 개별 저장이 아닌 배치 저장을 사용합니다.
     *
     * @param cases 분류된 케이스들
     * @return 생성된 PlayerCore 맵 (apiId -> PlayerCore)
     */
    private fun createPlayerCoresBatch(cases: PlayerProcessingCases): Map<Long, PlayerCore> {
        val playerCoreCreatePairs = mutableListOf<Pair<Long, PlayerApiSportsCreateDto>>()

        // 새로 생성할 PlayerCore들 (bothNewDtos)
        playerCoreCreatePairs.addAll(cases.bothNewDtos.filter { it.apiId != null }.map { it.apiId!! to it })

        // Core 연결이 필요한 PlayerCore들 (apiOnlyPlayers)
        playerCoreCreatePairs.addAll(
            cases.apiOnlyPlayers.map { it.first.apiId to it.second }.filter { it.first != null }.map {
                it.first!! to
                    it.second
            },
        )

        return if (playerCoreCreatePairs.isNotEmpty()) {
            playerCoreSyncService.createPlayerCoresFromApiSports(playerCoreCreatePairs)
        } else {
            emptyMap()
        }
    }

    /**
     * STEP 3: Team-Player 연관관계 배치 생성
     *
     * 새로 생성된 PlayerCore들과 TeamCore 간의 연관관계를 배치로 생성합니다.
     *
     * @param teamCore 연관관계를 생성할 TeamCore 엔티티
     * @param playerCoreMap 생성된 PlayerCore 맵
     */
    private fun createTeamPlayerRelationshipsBatch(
        teamCore: TeamCore,
        playerCoreMap: Map<Long, PlayerCore>,
    ) {
        if (playerCoreMap.values.isNotEmpty()) {
            teamPlayerCoreSyncService.createTeamPlayerRelationshipsBatch(teamCore, playerCoreMap.values.toList())
        }
    }

    /**
     * STEP 4: PlayerApiSports 엔티티 준비
     *
     * 모든 PlayerApiSports 엔티티를 준비합니다:
     * - 기존 엔티티들 (bothExistPlayers, apiOnlyPlayers)
     * - 새로 생성할 엔티티들 (bothNewDtos)
     *
     * @param cases 분류된 케이스들
     * @param playerCoreMap 생성된 PlayerCore 맵
     * @return 모든 PlayerApiSports 엔티티 맵 (apiId -> PlayerApiSports)
     */
    private fun prepareAllPlayerApiSports(
        cases: PlayerProcessingCases,
        playerCoreMap: Map<Long, PlayerCore>,
    ): Map<Long, PlayerApiSports> {
        val allPlayerApiSports = mutableMapOf<Long, PlayerApiSports>()

        // 기존 엔티티들 추가
        cases.bothExistPlayers.forEach { player ->
            player.apiId?.let { allPlayerApiSports[it] = player }
        }

        // Core 연결이 필요한 엔티티들 처리
        cases.apiOnlyPlayers.forEach { (playerApiSports, dto) ->
            val playerCore = playerCoreMap[playerApiSports.apiId]
            if (playerCore != null) {
                playerApiSports.playerCore = playerCore
            }
            playerApiSports.apiId?.let { allPlayerApiSports[it] = playerApiSports }
        }

        // 새로 생성할 엔티티들 생성 (비영속 상태)
        cases.bothNewDtos.forEach { dto ->
            val playerCore = playerCoreMap[dto.apiId]
            if (playerCore != null && dto.apiId != null) {
                val newPlayerApiSports =
                    PlayerApiSports(
                        playerCore = playerCore,
                        apiId = dto.apiId,
                        name = dto.name,
                        firstname = dto.firstname,
                        lastname = dto.lastname,
                        age = dto.age,
                        nationality = dto.nationality,
                        number = dto.number,
                        position = dto.position,
                        photo = dto.photo,
                    )
                allPlayerApiSports[dto.apiId] = newPlayerApiSports
            }
        }

        return allPlayerApiSports
    }

    /**
     * STEP 5: 기존 PlayerApiSports 엔티티 기본 정보 업데이트
     *
     * 기존 PlayerApiSports 엔티티들의 기본 정보를 DTO에서 업데이트합니다.
     * preventUpdate 플래그가 true인 경우 업데이트하지 않습니다.
     *
     * @param allPlayerApiSports 모든 PlayerApiSports 엔티티 맵
     * @param playerDtos 원본 선수 DTO 목록
     */
    private fun updateExistingPlayerApiSports(
        allPlayerApiSports: Map<Long, PlayerApiSports>,
        playerDtos: List<PlayerApiSportsCreateDto>,
    ) {
        val playerDtosMap = playerDtos.associateBy { it.apiId }

        allPlayerApiSports.values.forEach { playerApiSports ->
            val dto = playerDtosMap[playerApiSports.apiId]
            if (dto != null && !playerApiSports.preventUpdate) {
                playerApiSports.apply {
                    name = dto.name
                    firstname = dto.firstname
                    lastname = dto.lastname
                    age = dto.age
                    nationality = dto.nationality
                    number = dto.number
                    position = dto.position
                    photo = dto.photo
                }
            }
        }
    }

    /**
     * STEP 6: 최종 배치 저장
     *
     * 모든 PlayerApiSports 엔티티를 한 번에 배치로 저장합니다.
     *
     * @param allPlayerApiSports 모든 PlayerApiSports 엔티티 맵
     * @return 저장된 PlayerApiSports 엔티티 목록
     */
    private fun saveAllPlayerApiSports(allPlayerApiSports: Map<Long, PlayerApiSports>): List<PlayerApiSports> = playerApiRepository.saveAll(allPlayerApiSports.values)

    /**
     * PlayerApiSports 엔티티 처리와 동시에 PlayerCore를 수집합니다. (N+1 방지용)
     *
     * @param playerDtos 처리할 선수 DTO 목록
     * @param existingPlayerApiSportsMap 기존 PlayerApiSports 엔티티 맵
     * @param teamCore 연관관계를 생성할 TeamCore 엔티티 (null 가능)
     * @param playerCoreCollector PlayerCore 수집기
     * @return 처리된 PlayerApiSports 엔티티 목록
     */
    private fun processPlayerApiSportsEntitiesWithCoreCollection(
        playerDtos: List<PlayerApiSportsCreateDto>,
        existingPlayerApiSportsMap: Map<Long, PlayerApiSports>,
        teamCore: TeamCore?,
        playerCoreCollector: MutableList<PlayerCore>,
    ): List<PlayerApiSports> {
        if (existingPlayerApiSportsMap.isEmpty()) {
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
                val newPlayerApiSports = createNewPlayerApiSports(dto, teamCore)
                result.add(newPlayerApiSports)

                // PlayerCore 수집 (방금 생성된 것, 메모리에 있음)
                newPlayerApiSports.playerCore?.let { playerCoreCollector.add(it) }
            }
        }

        val savedEntities = playerApiRepository.saveAll(result)
        log.info(
            "Saved ${savedEntities.size} PlayerApiSports entities, collected ${playerCoreCollector.size} PlayerCore entities",
        )

        return savedEntities
    }

    /**
     * 기존 PlayerApiSports 엔티티 업데이트
     */
    private fun updateExistingPlayerApiSports(
        playerApiSports: PlayerApiSports,
        dto: PlayerApiSportsCreateDto,
    ): PlayerApiSports {
        // 기본 정보 업데이트
        playerApiSports.apply {
            name = dto.name
            firstname = dto.firstname
            lastname = dto.lastname
            age = dto.age
            nationality = dto.nationality
            number = dto.number
            position = dto.position
            photo = dto.photo
        }

        // PlayerCore가 없는 비정상 케이스 처리
        if (playerApiSports.playerCore == null) {
            val newPlayerCore = createPlayerCore(dto)
            val savedPlayerCore = playerCoreSyncService.savePlayerCore(dto)
            playerApiSports.playerCore = savedPlayerCore
        }

        return playerApiSports
    }

    /**
     * 새로운 PlayerApiSports 엔티티 생성
     * TeamCore가 null인 경우, 연관관계 설정 없이 PlayerApiSports만 생성합니다.
     */
    private fun createNewPlayerApiSports(
        dto: PlayerApiSportsCreateDto,
        teamCore: TeamCore?,
    ): PlayerApiSports {
        // 1. PlayerCore 생성
        val savedPlayerCore = playerCoreSyncService.savePlayerCore(dto)

        // 2. TeamCore와 PlayerCore 연관관계 생성
        if (teamCore != null) {
            teamPlayerCoreSyncService.createTeamPlayerRelationship(teamCore, savedPlayerCore)
        }

        // 3. PlayerApiSports 생성 및 연관관계 설정
        return PlayerApiSports(
            playerCore = savedPlayerCore,
            apiId = dto.apiId,
            name = dto.name,
            firstname = dto.firstname,
            lastname = dto.lastname,
            age = dto.age,
            nationality = dto.nationality,
            number = dto.number,
            position = dto.position,
            photo = dto.photo,
        )
    }

    /**
     * PlayerCore 엔티티 생성
     */
    private fun createPlayerCore(dto: PlayerApiSportsCreateDto): PlayerCore = playerCoreSyncService.savePlayerCore(dto)
}
