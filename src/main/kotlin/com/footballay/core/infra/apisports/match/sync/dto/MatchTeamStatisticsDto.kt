package com.footballay.core.infra.apisports.match.sync.dto

import java.math.BigDecimal

data class MatchTeamStatisticsDto(
    val teamApiId: Long,
    // 슈팅 관련 통계
    val shotsOnGoal: Int?,
    val shotsOffGoal: Int?,
    val totalShots: Int?,
    val blockedShots: Int?,
    val shotsInsideBox: Int?,
    val shotsOutsideBox: Int?,
    // 기타 경기 통계
    val fouls: Int?,
    val cornerKicks: Int?,
    val offsides: Int?,
    val ballPossession: String?, // "67%" 형태
    // 카드 관련 통계
    val yellowCards: Int?,
    val redCards: Int?,
    // 골키퍼 관련 통계
    val goalkeeperSaves: Int?,
    // 패스 관련 통계
    val totalPasses: Int?,
    val passesAccurate: Int?,
    val passesPercentage: String?, // "88%" 형태
    // 기대득점 관련 통계
    val goalsPrevented: Int?,
    /**
     * xg 값은 시간에 따라 변하는 값입니다.
     * 적절한 xg 값이 아직 제공되지 않은 경우 비어있을 수 있습니다.
     */
    val xgList: List<MatchTeamXGDto> = emptyList() // XG 리스트
) {
    data class MatchTeamXGDto(
        val xg: Double,
        val elapsed: Int,
    )
}