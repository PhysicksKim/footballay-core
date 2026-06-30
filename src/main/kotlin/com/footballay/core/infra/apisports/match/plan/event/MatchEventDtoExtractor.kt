package com.footballay.core.infra.apisports.match.plan.event

import com.footballay.core.infra.apisports.match.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventPlanDto

interface MatchEventDtoExtractor {
    /**
     * [FullMatchSyncDto] 를 받아서 [MatchEventDto] 로 이뤄진 [MatchEventPlanDto] 를 생성합니다.
     */
    fun extractEvents(
        dto: FullMatchSyncDto,
        context: MatchPlayerContext,
    ): MatchEventPlanDto
}
