package com.footballay.core.matchdata.sync.apisports.plan.teamstat

import com.footballay.core.matchdata.sync.apisports.FullMatchSyncDto
import com.footballay.core.matchdata.sync.apisports.plan.dto.MatchTeamStatPlanDto

interface MatchTeamStatDtoExtractor {
    fun extractTeamStats(dto: FullMatchSyncDto): MatchTeamStatPlanDto
}
