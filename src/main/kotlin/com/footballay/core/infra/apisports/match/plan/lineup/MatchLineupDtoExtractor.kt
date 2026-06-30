package com.footballay.core.infra.apisports.match.plan.lineup

import com.footballay.core.infra.apisports.match.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.dto.MatchLineupPlanDto

interface MatchLineupDtoExtractor {
    fun extractLineup(
        dto: FullMatchSyncDto,
        context: MatchPlayerContext,
    ): MatchLineupPlanDto
}
