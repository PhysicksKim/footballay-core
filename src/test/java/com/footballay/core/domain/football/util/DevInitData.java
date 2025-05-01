package com.footballay.core.domain.football.util;

import com.footballay.core.domain.football.FootballRoot;
import com.footballay.core.domain.football.constant.FixtureId;
import com.footballay.core.domain.football.constant.LeagueId;
import com.footballay.core.domain.football.constant.TeamId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DevInitData {

    private final FootballRoot footballRoot;

    public void addData() {
        // Save [ _League, _Team, _Player, _Fixture ] of Euro 2024
        cacheLeagueTeamPlayerFixtureOfEuro2024();

        // Save [ AvailableLeague, AvailableFixture ] of Euro 2024
        cacheAvailableLeagueAndAvailableFixtureOfEuro2024();
    }

    private void cacheLeagueTeamPlayerFixtureOfEuro2024() {
        footballRoot.cacheLeagueById(LeagueId.EURO);
        footballRoot.cacheTeamsOfLeague(LeagueId.EURO);
        for (Long teamId : TeamId.EURO2024TEAMS) {
            footballRoot.cacheSquadOfTeam(teamId);
        }
        footballRoot.cacheAllFixturesOfLeague(LeagueId.EURO);
    }

    private void cacheAvailableLeagueAndAvailableFixtureOfEuro2024() {
        cacheAvailableLeague(LeagueId.EURO);
        cacheAvailableFixture(LeagueId.EURO);
    }

    private void cacheAvailableLeague(long leagueId) {
        footballRoot.addAvailableLeague(leagueId);
    }

    private void cacheAvailableFixture(long leagueId) {
        if(leagueId == LeagueId.EURO)
            footballRoot.addAvailableFixture(FixtureId.FIXTURE_EURO2024_1);
    }

}
