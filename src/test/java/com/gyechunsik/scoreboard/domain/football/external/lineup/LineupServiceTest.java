package com.gyechunsik.scoreboard.domain.football.external.lineup;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@ActiveProfiles({"dev", "mockapi"})
@SpringBootTest
class LineupServiceTest {

    @Autowired
    LineupService lineupService;

    @Autowired
    private ApiCallService apiCallService;
    @Autowired
    private FootballApiCacheService footballApiCacheService;
    @Autowired
    private FixtureRepository fixtureRepository;
    @Autowired
    private MatchLineupRepository matchLineupRepository;
    @Autowired
    private MatchPlayerRepository matchPlayerRepository;

    @Transactional
    @DisplayName("singleFixture 응답으로 MatchLineup 과 MatchPlayer 저장 성공")
    @Test
    void singleFixture() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        List<Fixture> fixtures = footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());

        // when
        log.info("BEFORE fixture single response API request");
        FixtureSingleResponse fixtureSingleResponse = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);

        lineupService.saveLineup(fixtureSingleResponse);

        // then
        fixtureRepository.findById(FixtureId.FIXTURE_SINGLE_1145526).ifPresent(fixture -> {
            log.info("fixture :: {}", fixture);
        });
        List<MatchLineup> all = matchLineupRepository.findAll();
        Fixture fixture = fixtureRepository.findById(FixtureId.FIXTURE_SINGLE_1145526).orElseThrow();
        MatchLineup homeMatchLineup = matchLineupRepository.findByFixtureAndTeam(fixture, fixture.getHomeTeam()).orElseThrow();
        MatchLineup awayMatchLineup = matchLineupRepository.findByFixtureAndTeam(fixture, fixture.getAwayTeam()).orElseThrow();
        log.info("homeMatchLineup :: {}", homeMatchLineup);

        List<MatchPlayer> homeLineupPlayers = matchPlayerRepository.findByMatchLineup(homeMatchLineup);
        homeLineupPlayers.forEach(startPlayer -> {
            log.info("startPlayer :: (id={},name={},isSub={})",
                    startPlayer.getPlayer().getId(),
                    startPlayer.getPlayer().getName(),
                    startPlayer.getSubstitute()
            );
        });
        List<MatchPlayer> awayLineupPlayers = matchPlayerRepository.findByMatchLineup(awayMatchLineup);
        awayLineupPlayers.forEach(startPlayer -> {
            log.info("startPlayer :: (id={},name={},isSub={})",
                    startPlayer.getPlayer().getId(),
                    startPlayer.getPlayer().getName(),
                    startPlayer.getSubstitute()
            );
        });

        // assert
        assertThat(all.size()).isEqualTo(2);
        assertThat(homeLineupPlayers).size().isGreaterThan(11);
        assertThat(awayLineupPlayers).size().isGreaterThan(11);
    }
}