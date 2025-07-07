package com.footballay.core.infra.apisports.syncer.match.event

import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.syncer.match.MatchSyncResult
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.infra.apisports.syncer.match.dto.MatchEventSyncDto

interface MatchEventSync {
    fun syncEvents(dto: FullMatchSyncDto, context: MatchPlayerContext): MatchEventSyncDto
}