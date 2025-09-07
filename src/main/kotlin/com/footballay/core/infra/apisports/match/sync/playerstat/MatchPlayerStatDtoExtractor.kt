package com.footballay.core.infra.apisports.match.sync.playerstat

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.PlayerStatSyncDto

interface MatchPlayerStatDtoExtractor {
    fun extractPlayerStats(dto: FullMatchSyncDto, context: MatchPlayerContext): PlayerStatSyncDto
}