package com.footballay.core.infra.apisports.match.persist.playerstat.planner

import com.footballay.core.infra.apisports.match.persist.playerstat.dto.PlayerStatsDto
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayerStatistics
import com.footballay.core.logger

/**
 * PlayerStats 변경 계획 수립기
 *
 * 새로운 PlayerStatsDto와 기존 PlayerStats 엔티티를 비교하여 변경 계획을 수립합니다.
 *
 * **핵심 로직:**
 * 1. 기존 통계를 키 기반으로 맵핑
 * 2. 새로운 통계와 기존 통계 비교
 * 3. 생성/수정/삭제 계획 수립
 *
 * **중요한 제약사항:**
 * - 모든 MatchPlayer는 Phase 3에서 이미 영속화됨
 * - 통계 전용 MatchPlayer는 Phase 3에서 이미 생성됨
 * - 1:1 관계 보장
 */
object PlayerStatsChangePlanner {
    private val log = logger()

    /**
     * 기존 PlayerStats 엔티티들을 키 기반으로 맵핑합니다.
     *
     * **핵심 로직:**
     * - MatchPlayer 키를 기준으로 맵핑
     * - 기존 통계가 없는 경우 null로 처리
     *
     * @param existingStats 기존 PlayerStats 엔티티 목록
     * @return 키 기반 맵핑된 기존 통계 맵
     */
    fun entitiesToKeyMap(
        existingStats: List<ApiSportsMatchPlayerStatistics>,
    ): Map<String, ApiSportsMatchPlayerStatistics> =
        existingStats
            .associate { stats ->
                val key =
                    MatchPlayerKeyGenerator.generateMatchPlayerKey(
                        stats.matchPlayer?.playerApiSports?.apiId,
                        stats.matchPlayer?.name ?: "unknown",
                    )
                key to stats
            }.also {
                log.debug("Mapped {} existing player statistics to {} keys", existingStats.size, it.size)
            }

    /**
     * 새로운 통계와 기존 통계를 비교하여 변경 계획을 수립합니다.
     *
     * **핵심 로직:**
     * 1. 새로운 통계에 대해 기존 통계 존재 여부 확인
     * 2. 기존 통계가 있으면 수정, 없으면 생성
     * 3. 새로운 통계에 없는 기존 통계는 삭제
     *
     * **중요한 비즈니스 로직:**
     * - MatchPlayer가 존재하지 않는 통계는 제외
     * - 통계 데이터 무결성 검증
     * - 1:1 관계 보장
     *
     * @param newStats 새로운 PlayerStatsDto 목록
     * @param existingStatsMap 기존 통계 맵 (키 기반)
     * @param matchPlayers 영속화된 MatchPlayer 맵
     * @return 변경 계획
     */
    fun planChanges(
        newStats: List<PlayerStatsDto>,
        existingStatsMap: Map<String, ApiSportsMatchPlayerStatistics>,
        matchPlayers: Map<String, ApiSportsMatchPlayer>,
    ): PlayerStatsChangeSet {
        val toCreate = mutableListOf<PlayerStatsDto>()
        val toUpdate = mutableListOf<Pair<ApiSportsMatchPlayerStatistics, PlayerStatsDto>>()
        val toDelete = mutableListOf<ApiSportsMatchPlayerStatistics>()

        // 새로운 통계 처리
        newStats.forEach { newStat ->
            val existingStat = existingStatsMap[newStat.playerKey]

            // MatchPlayer 존재 여부 확인
            val matchPlayer = matchPlayers[newStat.playerKey]
            if (matchPlayer == null) {
                log.warn("MatchPlayer not found for statistics: {}, skipping", newStat.playerKey)
                return@forEach
            }

            if (existingStat == null) {
                // 기존 통계가 없으면 생성
                toCreate.add(newStat)
                log.debug("Planned to create statistics for: {} ({})", matchPlayer.name, newStat.playerKey)
            } else {
                // 기존 통계가 있으면 수정
                toUpdate.add(existingStat to newStat)
                log.debug("Planned to update statistics for: {} ({})", matchPlayer.name, newStat.playerKey)
            }
        }

        // 삭제할 통계 처리 (새로운 통계에 없는 기존 통계)
        val newStatKeys = newStats.map { it.playerKey }.toSet()
        existingStatsMap.forEach { (key, existingStat) ->
            if (!newStatKeys.contains(key)) {
                toDelete.add(existingStat)
                log.debug("Planned to delete statistics for: {} ({})", existingStat.matchPlayer?.name, key)
            }
        }

        log.info("Planned changes - Create: {}, Update: {}, Delete: {}", toCreate.size, toUpdate.size, toDelete.size)

        return PlayerStatsChangeSet(
            toCreate = toCreate,
            toUpdate = toUpdate,
            toDelete = toDelete,
        )
    }
}
