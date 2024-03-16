package com.gyechunsik.scoreboard.domain.football.data.fetch;

import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.TeamInfoResponse;

import java.io.IOException;

public interface ApiCallService {
    LeagueInfoResponse leagueInfo(long leagueId);

    TeamInfoResponse teamInfo(long teamId);

    PlayerSquadResponse playerSquad(long teamId);
}
