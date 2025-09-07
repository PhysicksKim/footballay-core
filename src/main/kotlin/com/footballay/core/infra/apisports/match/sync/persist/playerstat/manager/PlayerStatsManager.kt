package com.footballay.core.infra.apisports.match.sync.persist.playerstat.manager

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.apisports.match.sync.dto.PlayerStatSyncDto
import com.footballay.core.infra.apisports.match.sync.persist.playerstat.collector.PlayerStatsDtoCollector
import com.footballay.core.infra.apisports.match.sync.persist.playerstat.dto.PlayerStatsDto
import com.footballay.core.infra.apisports.match.sync.persist.playerstat.planner.PlayerStatsChangePlanner
import com.footballay.core.infra.apisports.match.sync.persist.playerstat.planner.PlayerStatsChangeSet
import com.footballay.core.infra.apisports.match.sync.persist.playerstat.result.PlayerStatsProcessResult
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayerStatistics
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchPlayerStatisticsRepository
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * PlayerStats 통합 관리자
 * 
 * PlayerStats의 수집, 계획, 저장을 통합하여 관리합니다.
 * 
 * **처리 과정:**
 * 1. PlayerStatsDtoCollector로 MatchPlayer 기반 수집
 * 2. PlayerStatsChangePlanner로 변경 계획 수립
 * 3. MatchPlayer 연결 및 영속 상태 저장
 * 4. EntityBundle 업데이트
 * 
 * **특징:**
 * - 영속 상태 PlayerStats를 EntityBundle에 반영
 * - MatchPlayer와 1:1 관계 유지
 * - 단일 책임으로 MatchEntitySyncServiceImpl 단순화
 * - 배치 처리로 성능 최적화
 * 
 * **중요한 비즈니스 로직:**
 * - ID null 선수 매칭 실패 시 substitute=true로 설정
 * - MatchPlayer와의 1:1 관계 보장
 * - 통계 데이터 무결성 검증
 */
@Component
class PlayerStatsManager(
    private val playerStatsRepository: ApiSportsMatchPlayerStatisticsRepository
) {

    private val log = logger()

    /**
     * PlayerStats를 MatchPlayer와 연결하여 수집, 계획, 저장하여 영속 상태로 만듭니다.
     * 
     * **핵심 로직:**
     * 1. MatchPlayer 기반으로 통계 데이터 수집
     * 2. 기존 통계와 비교하여 변경 계획 수립
     * 3. MatchPlayer 연결 및 배치 저장
     * 4. EntityBundle 업데이트
     * 
     * **중요한 제약사항:**
     * - 모든 MatchPlayer는 Phase 3에서 이미 영속화됨
     * - EntityBundle에서 MatchPlayer 조회 (Repository 사용 금지)
     * - 통계 전용 MatchPlayer는 Phase 3에서 이미 생성됨
     * 
     * @param playerStatDto PlayerStats 정보 DTO
     * @param entityBundle 기존 엔티티 번들 (업데이트됨)
     * @return PlayerStats 처리 결과
     */
    @Transactional
    fun processPlayerStats(
        playerStatDto: PlayerStatSyncDto,
        entityBundle: MatchEntityBundle
    ): PlayerStatsProcessResult {
        log.info("Starting PlayerStats processing - Home stats: ${playerStatDto.homePlayerStatList.size}, Away stats: ${playerStatDto.awayPlayerStatList.size}")
        
        try {
            // 1단계: MatchPlayer 기반으로 통계 데이터 수집
            val collectedStatsList = PlayerStatsDtoCollector.collectFrom(playerStatDto, entityBundle.allMatchPlayers)
            log.info("Collected ${collectedStatsList.size} player statistics from DTO")
            
            // 2단계: 변경 계획 수립
            val existingStatsMap = PlayerStatsChangePlanner.entitiesToKeyMap(entityBundle.getAllPlayerStats().values.toList())
            val statsChangeSet = PlayerStatsChangePlanner.planChanges(
                collectedStatsList,
                existingStatsMap,
                entityBundle.allMatchPlayers
            )
            log.info("Planned changes - Create: ${statsChangeSet.createCount}, Update: ${statsChangeSet.updateCount}, Delete: ${statsChangeSet.deleteCount}")
            
            // 3단계: MatchPlayer 연결 및 영속 상태 저장
            val savedStats = persistChangesWithMatchPlayerConnection(
                statsChangeSet,
                entityBundle.allMatchPlayers
            )
            
            // 4단계: EntityBundle 업데이트 (MatchPlayer.statistics 필드에 반영)
            savedStats.forEach { savedStat ->
                val matchPlayer = savedStat.matchPlayer
                if (matchPlayer != null) {
                    val key = MatchPlayerKeyGenerator.generateMatchPlayerKey(
                        matchPlayer.playerApiSports?.apiId,
                        matchPlayer.name
                    )
                    entityBundle.setPlayerStats(key, savedStat)
                    log.debug("Updated EntityBundle with PlayerStats: ${matchPlayer.name} (${savedStat.id})")
                }
            }
            
            log.info("PlayerStats processing completed - Total saved: ${savedStats.size}")
            return PlayerStatsProcessResult(
                totalStats = savedStats.size,
                createdCount = statsChangeSet.createCount,
                updatedCount = statsChangeSet.updateCount,
                deletedCount = statsChangeSet.deleteCount,
                savedStats = savedStats
            )
        } catch (e: Exception) {
            log.error("Failed to process PlayerStats", e)
            throw e
        }
    }

    /**
     * MatchPlayer 연결과 함께 변경 계획을 실제 데이터베이스에 반영합니다.
     * 
     * **핵심 로직:**
     * 1. 생성할 통계: MatchPlayer 연결 후 저장
     * 2. 수정할 통계: 기존 통계 업데이트
     * 3. 삭제할 통계: 데이터베이스에서 제거
     * 4. 배치 처리로 성능 최적화
     * 
     * **중요한 제약사항:**
     * - 모든 MatchPlayer는 이미 영속화된 상태
     * - EntityBundle에서 MatchPlayer 조회
     * - 1:1 관계 보장
     * 
     * **JPA 연관관계 설정:**
     * - PlayerStats를 먼저 영속화한 후 MatchPlayer와 연결
     * - 양방향 연관관계 보장
     * 
     * @param statsChangeSet 변경 계획
     * @param matchPlayers 영속화된 MatchPlayer 맵
     * @return 저장된 PlayerStats 목록
     */
    private fun persistChangesWithMatchPlayerConnection(
        statsChangeSet: PlayerStatsChangeSet,
        matchPlayers: Map<String, ApiSportsMatchPlayer>
    ): List<ApiSportsMatchPlayerStatistics> {
        val allStats = mutableListOf<ApiSportsMatchPlayerStatistics>()
        
        // 1. 삭제 처리 (기존 로직 유지)
        if (statsChangeSet.toDelete.isNotEmpty()) {
            playerStatsRepository.deleteAll(statsChangeSet.toDelete)
            log.info("Deleted ${statsChangeSet.toDelete.size} player statistics")
        }
        
        // 2. 생성할 통계: MatchPlayer 연결 후 저장
        val statsToCreate = statsChangeSet.toCreate.map { statsDto ->
            val matchPlayer = findMatchPlayerByKey(statsDto.playerKey, matchPlayers)
            if (matchPlayer == null) {
                log.warn("MatchPlayer not found for key: ${statsDto.playerKey}, skipping statistics creation")
                return@map null
            }
            
            // PlayerStats 생성 (비영속 상태)
            val playerStats = StatsEntityFrom(matchPlayer, statsDto)
            
            log.debug("Created player statistics for: ${matchPlayer.name} (${statsDto.playerKey})")
            playerStats
        }.filterNotNull()
        
        // 3. 수정할 통계: 기존 통계 업데이트
        val statsToUpdate = statsChangeSet.toUpdate.map { (existingStats, statsDto) ->
            updateStats(existingStats, statsDto)
            
            log.debug("Updated player statistics for: ${existingStats.matchPlayer?.name} (${statsDto.playerKey})")
            existingStats
        }
        
        // 4. 배치 저장 (성능 최적화)
        val allStatsToSave = statsToCreate + statsToUpdate
        if (allStatsToSave.isNotEmpty()) {
            val savedStats = playerStatsRepository.saveAll(allStatsToSave)
            log.info("Saved ${savedStats.size} player statistics (Create: ${statsToCreate.size}, Update: ${statsToUpdate.size})")
            
            // 5. 영속화 후 MatchPlayer와 연관관계 설정
            savedStats.forEach { savedStat ->
                val matchPlayer = savedStat.matchPlayer
                if (matchPlayer != null) {
                    // 양방향 연관관계 설정
                    matchPlayer.statistics = savedStat
                    log.debug("Set bidirectional relationship: ${matchPlayer.name} <-> ${savedStat.id}")
                }
            }
            
            allStats.addAll(savedStats)
        }
        
        return allStats
    }

    private fun updateStats(
        existingStats: ApiSportsMatchPlayerStatistics,
        statsDto: PlayerStatsDto
    ) {
        existingStats.apply {
            minutesPlayed = statsDto.minutesPlayed
            shirtNumber = statsDto.shirtNumber
            position = statsDto.position
            rating = statsDto.rating
            isCaptain = statsDto.isCaptain
            isSubstitute = statsDto.isSubstitute
            offsides = statsDto.offsides
            shotsTotal = statsDto.shotsTotal
            shotsOnTarget = statsDto.shotsOnTarget
            goalsTotal = statsDto.goalsTotal
            goalsConceded = statsDto.goalsConceded
            assists = statsDto.assists
            saves = statsDto.saves
            passesTotal = statsDto.passesTotal
            keyPasses = statsDto.keyPasses
            passesAccuracy = statsDto.passesAccuracy
            tacklesTotal = statsDto.tacklesTotal
            blocks = statsDto.blocks
            interceptions = statsDto.interceptions
            duelsTotal = statsDto.duelsTotal
            duelsWon = statsDto.duelsWon
            dribblesAttempts = statsDto.dribblesAttempts
            dribblesSuccess = statsDto.dribblesSuccess
            dribblesPast = statsDto.dribblesPast
            foulsDrawn = statsDto.foulsDrawn
            foulsCommitted = statsDto.foulsCommitted
            yellowCards = statsDto.yellowCards
            redCards = statsDto.redCards
            penaltyWon = statsDto.penaltyWon
            penaltyCommitted = statsDto.penaltyCommitted
            penaltyScored = statsDto.penaltyScored
            penaltyMissed = statsDto.penaltyMissed
            penaltySaved = statsDto.penaltySaved
        }
    }

    private fun StatsEntityFrom(
        matchPlayer: ApiSportsMatchPlayer,
        statsDto: PlayerStatsDto
    ): ApiSportsMatchPlayerStatistics = ApiSportsMatchPlayerStatistics(
        matchPlayer = matchPlayer,
        minutesPlayed = statsDto.minutesPlayed,
        shirtNumber = statsDto.shirtNumber,
        position = statsDto.position,
        rating = statsDto.rating,
        isCaptain = statsDto.isCaptain,
        isSubstitute = statsDto.isSubstitute,
        offsides = statsDto.offsides,
        shotsTotal = statsDto.shotsTotal,
        shotsOnTarget = statsDto.shotsOnTarget,
        goalsTotal = statsDto.goalsTotal,
        goalsConceded = statsDto.goalsConceded,
        assists = statsDto.assists,
        saves = statsDto.saves,
        passesTotal = statsDto.passesTotal,
        keyPasses = statsDto.keyPasses,
        passesAccuracy = statsDto.passesAccuracy,
        tacklesTotal = statsDto.tacklesTotal,
        blocks = statsDto.blocks,
        interceptions = statsDto.interceptions,
        duelsTotal = statsDto.duelsTotal,
        duelsWon = statsDto.duelsWon,
        dribblesAttempts = statsDto.dribblesAttempts,
        dribblesSuccess = statsDto.dribblesSuccess,
        dribblesPast = statsDto.dribblesPast,
        foulsDrawn = statsDto.foulsDrawn,
        foulsCommitted = statsDto.foulsCommitted,
        yellowCards = statsDto.yellowCards,
        redCards = statsDto.redCards,
        penaltyWon = statsDto.penaltyWon,
        penaltyCommitted = statsDto.penaltyCommitted,
        penaltyScored = statsDto.penaltyScored,
        penaltyMissed = statsDto.penaltyMissed,
        penaltySaved = statsDto.penaltySaved
    )

    /**
     * MatchPlayer 키로 MatchPlayer를 찾습니다.
     * 
     * **중요한 제약사항:**
     * - EntityBundle에서만 조회 (Repository 사용 금지)
     * - 모든 MatchPlayer는 Phase 3에서 이미 영속화됨
     * 
     * @param playerKey MatchPlayer 키
     * @param matchPlayers 영속화된 MatchPlayer 맵
     * @return 해당하는 MatchPlayer 또는 null
     */
    private fun findMatchPlayerByKey(
        playerKey: String,
        matchPlayers: Map<String, ApiSportsMatchPlayer>
    ): ApiSportsMatchPlayer? {
        return matchPlayers[playerKey]?.also {
            log.debug("Found MatchPlayer for key: $playerKey -> ${it.name}")
        } ?: run {
            log.warn("MatchPlayer not found for key: $playerKey")
            null
        }
    }
} 