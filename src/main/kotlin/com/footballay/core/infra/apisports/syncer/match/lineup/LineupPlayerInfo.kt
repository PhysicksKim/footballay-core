package com.footballay.core.infra.apisports.syncer.match.lineup

import com.footballay.core.infra.apisports.live.FullMatchSyncDto

/**
 * 라인업 선수 정보 (substitute 여부와 id=null 여부 포함)
 */
data class LineupPlayerInfo(
    val player: FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto,
    val isSubstitute: Boolean,
    val teamId: Long
)