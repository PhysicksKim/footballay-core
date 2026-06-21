package com.footballay.core.web.admin.core.dto

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import java.time.Instant

data class MatchCollectStateResponse(
    val fixtureUid: String,
    val leagueUid: String?,
    val seasonYear: Int?,
    val currentSeason: Boolean?,
    val kickoff: Instant?,
    val fixtureStatusCode: FixtureStatusCode,
    val fixtureAvailable: Boolean,
    val leagueMatchCollect: MatchCollect?,
    val matchCollectStatus: MatchCollectStatus,
    val lastCollectedAt: Instant?,
)

data class MatchCollectStatePageResponse(
    val content: List<MatchCollectStateResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
