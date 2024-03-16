package com.gyechunsik.scoreboard.domain.football.data;

import com.gyechunsik.scoreboard.domain.football.data.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse;
import com.gyechunsik.scoreboard.domain.football.fixture.Fixture;
import com.gyechunsik.scoreboard.domain.football.league.League;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FootballDataFacade {

    private final ApiCallService apiCallService;

    public boolean cachingLeague(long leagueId) {
        // api 에서 콜 하고
        // cache 전담 객체에 넘겨주고
        // 성공시 true 리턴
        // LeagueInfoResponse leagueInfoResponse = apiCallService.fetchLeague(leagueId);

        return false;
    }

    // public void callFixture(String fixtureId) {
    //     // is Valid Record Fixture?
    //     Fixture response = retrieveFixture(fixtureId); // 없을시 IllegalArgumentException
    //
    //     // is Exist LeagueResponse, Team
    //     // LeagueResponse league = retrieveLeague(response.leagueId); // 없을 시 캐싱로직
    //     // Team teamHome = retrieveTeam(response.teamHome); // 없을 시 캐싱로직
    //     // Team teamAway = retrieveTeam(response.teamAway); // 없을 시 캐싱로직
    //
    //     // return fixtureData;
    // }

    // public Lineups callLineups(String fixtureId) {
    //     // is exist cachedLineups?
    //     // Optional<Lineups> optionalLineups = getLineups(fixtureId)
    //     //
    //     // if optionalLineups.isPresent()
    //     //      return optionalLineups.get()
    //     // else
    //     //      return retrieveLineupsWithCaching(fixtureId)
    // }

    /*
    private Lineups retrieveLineupsWithCaching(fixtureId) {
        // retrieve Lineups
        // LineupsResponse lineupsResponse = apiCallLineups(fixtureId) /

        // extract both Team
        // FixtureLineup home = extractAndKoreanTranslate(lineups.teams.home);
        // FixtureLineup away = extractAndKoreanTranslate(lineups.teams.away);

        // Combines to Lineups
        // return combineLineups(home, away);
    }
     */
}
