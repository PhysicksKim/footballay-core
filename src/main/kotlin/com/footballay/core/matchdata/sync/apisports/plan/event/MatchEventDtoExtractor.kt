package com.footballay.core.matchdata.sync.apisports.plan.event

import com.footballay.core.matchdata.sync.apisports.FullMatchSyncDto
import com.footballay.core.matchdata.sync.apisports.plan.context.MatchPlayerContext
import com.footballay.core.matchdata.sync.apisports.plan.dto.MatchEventDto
import com.footballay.core.matchdata.sync.apisports.plan.dto.MatchEventPlanDto

interface MatchEventDtoExtractor {
    /**
     * [FullMatchSyncDto] 를 받아서 [MatchEventDto] 로 이뤄진 [MatchEventPlanDto] 를 생성합니다.
     */
    fun extractEvents(
        dto: FullMatchSyncDto,
        context: MatchPlayerContext,
    ): MatchEventPlanDto
}
