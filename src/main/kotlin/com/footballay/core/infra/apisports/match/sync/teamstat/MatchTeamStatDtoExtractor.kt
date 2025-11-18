package com.footballay.core.infra.apisports.match.sync.teamstat

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchTeamStatPlanDto

interface MatchTeamStatDtoExtractor {
    fun extractTeamStats(dto: FullMatchSyncDto): MatchTeamStatPlanDto
}
