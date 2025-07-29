package com.footballay.core.infra.apisports.syncer.match.event

import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.infra.apisports.syncer.match.dto.MatchEventDto
import com.footballay.core.infra.apisports.syncer.match.dto.MatchEventSyncDto

interface MatchEventDtoExtractor {
    /**
     * [FullMatchSyncDto] 를 받아서 [MatchEventDto] 로 이뤄진 [MatchEventSyncDto] 를 생성합니다.
     */
    fun extractEvents(dto: FullMatchSyncDto, context: MatchPlayerContext): MatchEventSyncDto
}