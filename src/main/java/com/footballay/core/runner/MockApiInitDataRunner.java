package com.footballay.core.runner;

import com.footballay.core.domain.football.FootballRoot;
import com.footballay.core.domain.football.constant.FixtureId;
import com.footballay.core.domain.football.constant.LeagueId;
import com.footballay.core.domain.football.constant.TeamId;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * mockApi 사용 시 초기 데이터를 추가하는 Runner 입니다.
 */
@Component
@Profile("mockapi")
public class MockApiInitDataRunner implements ApplicationRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MockApiInitDataRunner.class);
    private final FootballRoot footballRoot;
    private static final boolean WANT_TO_ADD_DATA = false;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (WANT_TO_ADD_DATA) {
            addData();
        }
    }

    private void addData() {
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
        if (leagueId == LeagueId.EURO) footballRoot.addAvailableFixture(FixtureId.FIXTURE_EURO2024_1);
    }

    public MockApiInitDataRunner(final FootballRoot footballRoot) {
        this.footballRoot = footballRoot;
    }
}
