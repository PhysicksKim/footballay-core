package com.gyechunsik.scoreboard.domain.football.external.fetch;

import com.gyechunsik.scoreboard.domain.football.external.fetch.response.*;

public interface ApiCallService {

    ExternalApiStatusResponse status();

    LeagueInfoResponse leagueInfo(long leagueId);

    LeagueInfoResponse teamCurrentLeaguesInfo(long teamId);

    TeamInfoResponse teamInfo(long teamId);

    TeamInfoResponse teamsInfo(long leagueId, int currentSeason);

    PlayerSquadResponse playerSquad(long teamId);

    LeagueInfoResponse allLeagueCurrent();

    FixtureResponse fixturesOfLeagueSeason(long leagueId, int season);

    FixtureSingleResponse fixtureSingle(long fixtureId);

    PlayerInfoResponse playerSingle(long playerId, long leagueId, int season);
}
