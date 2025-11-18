package com.footballay.core.infra.apisports.match.persist.playerstat.collector

import com.footballay.core.infra.apisports.match.persist.playerstat.dto.PlayerStatsDto
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerStatPlanDto
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.logger

/**
 * PlayerStats DTO 수집기
 *
 * PlayerStatSyncDto에서 PlayerStatsDto를 추출하여 MatchPlayer와 연결합니다.
 *
 * **핵심 로직:**
 * 1. 홈팀/원정팀 통계 데이터 수집
 * 2. MatchPlayer 키 기반으로 매칭
 * 3. 통계 데이터를 PlayerStatsDto로 변환
 *
 * **중요한 제약사항:**
 * - 모든 MatchPlayer는 Phase 3에서 이미 영속화됨
 * - 통계 전용 MatchPlayer는 Phase 3에서 이미 생성됨
 * - 매칭 실패 시 해당 통계는 제외
 */
object PlayerStatsDtoCollector {
    private val log = logger()

    /**
     * PlayerStatSyncDto에서 PlayerStatsDto를 수집합니다.
     *
     * **처리 과정:**
     * 1. 홈팀 통계 데이터 처리
     * 2. 원정팀 통계 데이터 처리
     * 3. MatchPlayer 키 기반으로 매칭
     * 4. 매칭 성공한 통계만 PlayerStatsDto로 변환
     *
     * **중요한 비즈니스 로직:**
     * - ID null 선수 매칭 실패 시 해당 통계 제외
     * - MatchPlayer가 존재하지 않는 통계는 제외
     * - 통계 데이터 무결성 검증
     *
     * @param playerStatDto PlayerStats 정보 DTO
     * @param matchPlayers 영속화된 MatchPlayer 맵
     * @return 수집된 PlayerStatsDto 목록
     */
    fun collectFrom(
        playerStatDto: MatchPlayerStatPlanDto,
        matchPlayers: Map<String, ApiSportsMatchPlayer>,
    ): List<PlayerStatsDto> {
        val collectedStats = mutableListOf<PlayerStatsDto>()

        // 홈팀 통계 데이터 처리
        val homeStats =
            processTeamPlayerStats(
                playerStatDto.homePlayerStatList,
                matchPlayers,
                "홈팀",
            )
        collectedStats.addAll(homeStats)

        // 원정팀 통계 데이터 처리
        val awayStats =
            processTeamPlayerStats(
                playerStatDto.awayPlayerStatList,
                matchPlayers,
                "원정팀",
            )
        collectedStats.addAll(awayStats)

        log.info(
            "Collected ${collectedStats.size} player statistics (Home: ${homeStats.size}, Away: ${awayStats.size})",
        )

        return collectedStats
    }

    /**
     * 팀별 선수 통계 데이터를 처리합니다.
     *
     * **핵심 로직:**
     * 1. 각 선수 통계에 대해 MatchPlayer 키 생성
     * 2. MatchPlayer 존재 여부 확인
     * 3. 통계 데이터를 PlayerStatsDto로 변환
     *
     * **중요한 제약사항:**
     * - MatchPlayer가 존재하지 않는 통계는 제외
     * - ID null 선수 매칭 실패 시 해당 통계 제외
     *
     * @param teamPlayerStats 팀별 선수 통계 목록
     * @param matchPlayers 영속화된 MatchPlayer 맵
     * @param teamName 팀 이름 (로깅용)
     * @return 처리된 PlayerStatsDto 목록
     */
    private fun processTeamPlayerStats(
        teamPlayerStats: List<MatchPlayerStatPlanDto.PlayerStatSyncItemDto>,
        matchPlayers: Map<String, ApiSportsMatchPlayer>,
        teamName: String,
    ): List<PlayerStatsDto> {
        return teamPlayerStats.mapNotNull { playerStat ->
            // MatchPlayer 키 생성
            val playerKey =
                MatchPlayerKeyGenerator.generateMatchPlayerKey(
                    playerStat.playerApiId,
                    playerStat.name,
                )

            // MatchPlayer 존재 여부 확인
            val matchPlayer = matchPlayers[playerKey]
            if (matchPlayer == null) {
                log.warn("MatchPlayer not found for statistics: {} ({}) in {}", playerKey, playerStat.name, teamName)
                return@mapNotNull null
            }

            // 통계 데이터를 PlayerStatsDto로 변환
            val statsDto =
                PlayerStatsDto(
                    playerKey = playerKey,
                    minutesPlayed = playerStat.minutesPlayed,
                    shirtNumber = playerStat.shirtNumber,
                    position = playerStat.position,
                    rating = playerStat.rating,
                    isCaptain = playerStat.isCaptain ?: false,
                    isSubstitute = playerStat.isSubstitute ?: false,
                    offsides = playerStat.offsides,
                    shotsTotal = playerStat.shotsTotal,
                    shotsOnTarget = playerStat.shotsOnTarget,
                    goalsTotal = playerStat.goalsTotal,
                    goalsConceded = playerStat.goalsConceded,
                    assists = playerStat.assists,
                    saves = playerStat.saves,
                    passesTotal = playerStat.passesTotal,
                    keyPasses = playerStat.keyPasses,
                    passesAccuracy = playerStat.passesAccuracy,
                    tacklesTotal = playerStat.tacklesTotal,
                    blocks = playerStat.blocks,
                    interceptions = playerStat.interceptions,
                    duelsTotal = playerStat.duelsTotal,
                    duelsWon = playerStat.duelsWon,
                    dribblesAttempts = playerStat.dribblesAttempts,
                    dribblesSuccess = playerStat.dribblesSuccess,
                    dribblesPast = playerStat.dribblesPast,
                    foulsDrawn = playerStat.foulsDrawn,
                    foulsCommitted = playerStat.foulsCommitted,
                    yellowCards = playerStat.yellowCards,
                    redCards = playerStat.redCards,
                    penaltyWon = playerStat.penaltyWon,
                    penaltyCommitted = playerStat.penaltyCommitted,
                    penaltyScored = playerStat.penaltyScored,
                    penaltyMissed = playerStat.penaltyMissed,
                    penaltySaved = playerStat.penaltySaved,
                )

            log.debug("Collected statistics for: {} ({}) in {}", matchPlayer.name, playerKey, teamName)
            statsDto
        }
    }
}
