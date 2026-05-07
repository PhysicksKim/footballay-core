package com.footballay.core.matchdata.sync.apisports.plan.playerstat

import com.footballay.core.matchdata.sync.apisports.FullMatchSyncDto
import com.footballay.core.matchdata.sync.apisports.plan.context.MatchPlayerContext
import com.footballay.core.matchdata.sync.apisports.plan.dto.MatchPlayerStatPlanDto

interface MatchPlayerStatDtoExtractor {
    fun extractPlayerStats(
        dto: FullMatchSyncDto,
        context: MatchPlayerContext,
    ): MatchPlayerStatPlanDto
}
