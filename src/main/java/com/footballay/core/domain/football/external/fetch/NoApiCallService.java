package com.footballay.core.domain.football.external.fetch;

import com.footballay.core.domain.football.external.fetch.response.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("!mockapi & !api")
@Service
public class NoApiCallService implements ApiCallService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NoApiCallService.class);

    @Override
    public ExternalApiStatusResponse status() {
        log.info("No API Call Service. method : status");
        return null;
    }

    @Override
    public LeagueInfoResponse leagueInfo(long leagueId) {
        log.info("No API Call Service. method : leagueInfo");
        return null;
    }

    @Override
    public LeagueInfoResponse teamCurrentLeaguesInfo(long teamId) {
        log.info("No API Call Service. method : teamCurrentLeaguesInfo");
        return null;
    }

    @Override
    public TeamInfoResponse teamInfo(long teamId) {
        log.info("No API Call Service. method : teamInfo");
        return null;
    }

    @Override
    public TeamInfoResponse teamsInfo(long leagueId, int currentSeason) {
        log.info("No API Call Service. method : teamsInfo");
        return null;
    }

    @Override
    public PlayerSquadResponse playerSquad(long teamId) {
        log.info("No API Call Service. method : playerSquad");
        return null;
    }

    @Override
    public LeagueInfoResponse allLeagueCurrent() {
        log.info("No API Call Service. method : allLeagueCurrent");
        return null;
    }

    @Override
    public FixtureResponse fixturesOfLeagueSeason(long leagueId, int season) {
        log.info("No API Call Service. method : fixturesOfLeagueSeason");
        return null;
    }

    @Override
    public FixtureSingleResponse fixtureSingle(long fixtureId) {
        log.info("No API Call Service. method : fixtureSingle");
        return null;
    }

    @Override
    public PlayerInfoResponse playerSingle(long playerId, long leagueId, int season) {
        log.info("No API Call Service. method : playerSingle");
        return null;
    }

    @Override
    public StandingsResponse standings(long leagueId, int season) {
        log.info("No API Call Service. method : standing");
        return null;
    }
}
