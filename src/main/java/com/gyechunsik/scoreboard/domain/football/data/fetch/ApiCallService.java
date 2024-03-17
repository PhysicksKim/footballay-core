package com.gyechunsik.scoreboard.domain.football.data.fetch;

import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.TeamInfoResponse;

public interface ApiCallService {
    LeagueInfoResponse leagueInfo(long leagueId);

    LeagueInfoResponse teamCurrentLeaguesInfo(long teamId);

    TeamInfoResponse teamInfo(long teamId);

    PlayerSquadResponse playerSquad(long teamId);

}
