package com.footballay.core.domain.matchcollect

import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import org.springframework.stereotype.Component

@Component
class AdminMatchCollectQueryModelMapper {
    fun toStateModel(state: FixtureMatchCollectState): AdminMatchCollectStateModel {
        val fixture = state.fixture
        val season = fixture.leagueSeason

        @Suppress("DEPRECATION")
        val league = season?.league ?: fixture.league
        return AdminMatchCollectStateModel(
            fixtureUid = fixture.uid,
            leagueUid = league.uid,
            seasonYear = season?.seasonYear,
            currentSeason = season?.current,
            kickoff = fixture.kickoff,
            fixtureStatusCode = fixture.statusCode,
            fixtureAvailable = fixture.available,
            homeTeamName = fixture.homeTeam?.name,
            homeTeamNameKo = fixture.homeTeam?.nameKo,
            awayTeamName = fixture.awayTeam?.name,
            awayTeamNameKo = fixture.awayTeam?.nameKo,
            leagueMatchCollect = league.matchCollect,
            matchCollectStatus = state.matchCollectStatus,
            lastCollectedAt = state.lastCollectedAt,
        )
    }

    fun toLeagueModel(league: LeagueCore): AdminMatchCollectLeagueModel =
        AdminMatchCollectLeagueModel(
            leagueUid = league.uid,
            name = league.name,
            nameKo = league.nameKo,
            available = league.available,
            matchCollect = league.matchCollect,
        )
}
