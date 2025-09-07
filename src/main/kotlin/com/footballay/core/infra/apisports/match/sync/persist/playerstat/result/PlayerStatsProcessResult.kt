package com.footballay.core.infra.apisports.match.sync.persist.playerstat.result

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayerStatistics

/**
 * PlayerStats 처리 결과
 * 
 * PlayerStatsManager의 처리 결과를 담는 데이터 클래스입니다.
 * 
 * @param totalStats 총 처리된 통계 수
 * @param createdCount 새로 생성된 통계 수
 * @param updatedCount 수정된 통계 수
 * @param deletedCount 삭제된 통계 수
 * @param savedStats 저장된 PlayerStats 목록
 */
data class PlayerStatsProcessResult(
    val totalStats: Int,
    val createdCount: Int,
    val updatedCount: Int,
    val deletedCount: Int,
    val savedStats: List<ApiSportsMatchPlayerStatistics>
) 