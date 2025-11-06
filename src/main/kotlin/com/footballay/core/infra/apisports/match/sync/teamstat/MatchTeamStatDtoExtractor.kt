package com.footballay.core.infra.apisports.match.sync.teamstat

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.TeamStatSyncDto

interface MatchTeamStatDtoExtractor {
    fun extractTeamStats(dto: FullMatchSyncDto): TeamStatSyncDto
}
