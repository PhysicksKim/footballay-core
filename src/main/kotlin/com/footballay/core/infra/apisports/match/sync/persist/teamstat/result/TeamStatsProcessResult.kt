package com.footballay.core.infra.apisports.match.sync.persist.teamstat.result

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeamStatistics

/**
 * TeamStats 처리 결과
 *
 * Home/Away 각각의 TeamStats 처리 결과를 담습니다.
 *
 * **특징:**
 * - PlayerStats와 달리 복잡한 ChangeSet 불필요
 * - Home/Away 2개만 처리하므로 간단한 구조
 * - 생성/업데이트 여부만 추적
 */
data class TeamStatsProcessResult(
    val hasHome: Boolean,
    val hasAway: Boolean,
    val createdCount: Int,
    val updatedCount: Int,
    val homeTeamStat: ApiSportsMatchTeamStatistics?,
    val awayTeamStat: ApiSportsMatchTeamStatistics?,
) {
    companion object {
        /**
         * 빈 결과 생성 (DTO가 없는 경우)
         */
        fun empty(): TeamStatsProcessResult =
            TeamStatsProcessResult(
                hasHome = false,
                hasAway = false,
                createdCount = 0,
                updatedCount = 0,
                homeTeamStat = null,
                awayTeamStat = null,
            )
    }
}
