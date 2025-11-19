package com.footballay.core.infra.apisports.match.persist.playerstat.result

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayerStatistics

/**
 * PlayerStats 처리 결과
 *
 * PlayerStatsManager의 처리 결과를 담는 데이터 클래스입니다.
 *
 * @param totalStats 총 처리된 통계 수
 * @param createdCount 새로 생성된 통계 수
 * @param retainedCount DTO와 매칭되어 유지된 통계 수 (변경 여부와 무관)
 * @param deletedCount 삭제된 통계 수
 * @param savedStats 저장된 PlayerStats 목록
 */
data class PlayerStatsProcessResult(
    val totalStats: Int,
    val createdCount: Int,
    val retainedCount: Int,
    val deletedCount: Int,
    val savedStats: List<ApiSportsMatchPlayerStatistics>,
)
