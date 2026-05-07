package com.footballay.core.matchdata.sync.apisports.plan.lineup

import com.footballay.core.matchdata.sync.apisports.FullMatchSyncDto
import com.footballay.core.matchdata.sync.apisports.plan.context.MatchPlayerContext
import com.footballay.core.matchdata.sync.apisports.plan.dto.MatchLineupPlanDto

interface MatchLineupDtoExtractor {
    fun extractLineup(
        dto: FullMatchSyncDto,
        context: MatchPlayerContext,
    ): MatchLineupPlanDto
}
