package com.footballay.core.infra.apisports.match.plan.playerstat

import com.footballay.core.infra.apisports.match.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerStatPlanDto

interface MatchPlayerStatDtoExtractor {
    fun extractPlayerStats(
        dto: FullMatchSyncDto,
        context: MatchPlayerContext,
    ): MatchPlayerStatPlanDto
}
