package com.footballay.core.infra.apisports.match.sync.lineup

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.LineupSyncDto

interface MatchLineupDtoExtractor {
    fun extractLineup(dto: FullMatchSyncDto, context: MatchPlayerContext): LineupSyncDto
}