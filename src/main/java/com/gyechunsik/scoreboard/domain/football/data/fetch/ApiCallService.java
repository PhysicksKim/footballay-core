package com.gyechunsik.scoreboard.domain.football.data.fetch;

import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueCurrentResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.TeamInfoResponse;

public interface ApiCallService {

    LeagueInfoResponse leagueInfo(long leagueId);

    LeagueInfoResponse teamCurrentLeaguesInfo(long teamId);

    TeamInfoResponse teamInfo(long teamId);

    TeamInfoResponse teamsInfo(long leagueId, int currentSeason);

    PlayerSquadResponse playerSquad(long teamId);

    LeagueInfoResponse allLeagueCurrent();

}
