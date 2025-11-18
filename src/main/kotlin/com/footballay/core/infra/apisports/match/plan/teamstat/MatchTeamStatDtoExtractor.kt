package com.footballay.core.infra.apisports.match.plan.teamstat

import com.footballay.core.infra.apisports.match.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchTeamStatPlanDto

interface MatchTeamStatDtoExtractor {
    fun extractTeamStats(dto: FullMatchSyncDto): MatchTeamStatPlanDto
}
