package com.footballay.core.infra.apisports.match.sync.dto

data class MatchTeamStatPlanDto(
    /**
     * 팀 통계가 아직 주어지지 않은 경우 null
     */
    val homeStats: MatchTeamStatisticsDto? = null,
    /**
     * 팀 통계가 아직 주어지지 않은 경우 null
     */
    val awayStats: MatchTeamStatisticsDto? = null,
) {
    companion object {
        /**
         * 빈 팀 통계 DTO를 생성합니다.
         */
        fun empty(): MatchTeamStatPlanDto = MatchTeamStatPlanDto()
    }
}
