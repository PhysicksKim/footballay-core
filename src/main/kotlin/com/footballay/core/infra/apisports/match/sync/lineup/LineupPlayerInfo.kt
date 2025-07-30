package com.footballay.core.infra.apisports.match.sync.lineup

import com.footballay.core.infra.apisports.match.live.FullMatchSyncDto

/**
 * 라인업 선수 정보 (substitute 여부와 id=null 여부 포함)
 */
data class LineupPlayerInfo(
    val player: FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto,
    val isSubstitute: Boolean,
    val teamId: Long
)