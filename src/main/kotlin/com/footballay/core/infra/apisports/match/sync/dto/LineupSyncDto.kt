package com.footballay.core.infra.apisports.match.sync.dto

data class LineupSyncDto(
    val home: Lineup?,
    val away: Lineup?,
) {
    data class Lineup(
        val teamApiId: Long?,
        val teamName: String?,
        val teamLogo: String?,
        val playerColor: Color,
        val goalkeeperColor: Color,
        val formation: String?,
        /**
         * 해당 팀의 [com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator] 에서 생성한 MatchPlayer 키 목록입니다.
         * [com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext] 에서 실질적으로 값을 담은 DTO 또는 Entity 를 가져올 수 있습니다.
         */
        val startMpKeys: List<String>,
        val subMpKeys: List<String>,
    )

    data class Color(
        val primary: String?,
        val number: String?,
        val border: String?,
    )

    fun isEmpty(): Boolean = home == null || away == null

    companion object {
        val EMPTY = LineupSyncDto(null, null)
    }
}
