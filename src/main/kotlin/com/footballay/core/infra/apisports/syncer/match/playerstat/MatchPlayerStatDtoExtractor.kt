package com.footballay.core.infra.apisports.syncer.match.playerstat

import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.infra.apisports.syncer.match.dto.PlayerStatSyncDto

interface MatchPlayerStatDtoExtractor {
    fun extractPlayerStats(dto: FullMatchSyncDto, context: MatchPlayerContext): PlayerStatSyncDto
}