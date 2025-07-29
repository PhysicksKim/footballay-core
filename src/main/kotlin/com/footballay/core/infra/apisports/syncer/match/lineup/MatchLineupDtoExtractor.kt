package com.footballay.core.infra.apisports.syncer.match.lineup

import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.infra.apisports.syncer.match.dto.LineupSyncDto

interface MatchLineupDtoExtractor {
    fun extractLineup(dto: FullMatchSyncDto, context: MatchPlayerContext): LineupSyncDto
}