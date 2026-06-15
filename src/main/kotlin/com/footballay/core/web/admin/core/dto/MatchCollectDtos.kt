package com.footballay.core.web.admin.core.dto

import com.footballay.core.domain.league.MatchCollect
import jakarta.validation.constraints.NotNull

data class MatchCollectUpdateRequest(
    @field:NotNull
    val matchCollect: MatchCollect,
)

data class MatchCollectUpdateResponse(
    val uid: String,
    val matchCollect: MatchCollect,
    val reconcileSuccess: Boolean,
)
