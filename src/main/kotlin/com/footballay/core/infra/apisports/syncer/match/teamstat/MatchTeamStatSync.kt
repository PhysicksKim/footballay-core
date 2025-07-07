package com.footballay.core.infra.apisports.syncer.match.teamstat

import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.syncer.match.dto.TeamStatSyncDto

interface MatchTeamStatSync {
    fun syncTeamStats(dto: FullMatchSyncDto): TeamStatSyncDto
}
