package com.footballay.core.infra.apisports.match.sync.event

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventPlanDto

interface MatchEventDtoExtractor {
    /**
     * [FullMatchSyncDto] 를 받아서 [MatchEventDto] 로 이뤄진 [MatchEventPlanDto] 를 생성합니다.
     */
    fun extractEvents(
        dto: FullMatchSyncDto,
        context: MatchPlayerContext,
    ): MatchEventPlanDto
}
