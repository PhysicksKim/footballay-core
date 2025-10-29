package com.footballay.core.infra.apisports.match.sync.persist.playerstat.planner

import com.footballay.core.infra.apisports.match.sync.persist.playerstat.dto.PlayerStatsDto
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayerStatistics

/**
 * PlayerStats 변경 계획
 *
 * PlayerStatsChangePlanner에서 생성된 변경 계획을 담는 데이터 클래스입니다.
 *
 * @param toCreate 새로 생성할 PlayerStatsDto 목록
 * @param toUpdate 수정할 (기존 통계, 새 통계) 쌍 목록
 * @param toDelete 삭제할 기존 통계 목록
 * @param createCount 생성할 개수
 * @param updateCount 수정할 개수
 * @param deleteCount 삭제할 개수
 */
data class PlayerStatsChangeSet(
    val toCreate: List<PlayerStatsDto>,
    val toUpdate: List<Pair<ApiSportsMatchPlayerStatistics, PlayerStatsDto>>,
    val toDelete: List<ApiSportsMatchPlayerStatistics>,
    val createCount: Int = toCreate.size,
    val updateCount: Int = toUpdate.size,
    val deleteCount: Int = toDelete.size,
)
