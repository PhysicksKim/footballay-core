package com.footballay.core.domain.matchcollect

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import org.springframework.data.domain.Page
import java.time.Instant

data class AdminMatchCollectStateModel(
    val fixtureUid: String,
    val leagueUid: String?,
    val seasonYear: Int?,
    val currentSeason: Boolean?,
    val kickoff: Instant?,
    val fixtureStatusCode: FixtureStatusCode,
    val fixtureAvailable: Boolean,
    val homeTeamName: String?,
    val awayTeamName: String?,
    val leagueMatchCollect: MatchCollect?,
    val matchCollectStatus: MatchCollectStatus,
    val lastCollectedAt: Instant?,
)

data class AdminMatchCollectLeagueModel(
    val leagueUid: String,
    val name: String,
    val available: Boolean,
    val matchCollect: MatchCollect,
)

data class AdminMatchCollectLeagueStatePage(
    val league: AdminMatchCollectLeagueModel,
    val states: Page<AdminMatchCollectStateModel>,
)
