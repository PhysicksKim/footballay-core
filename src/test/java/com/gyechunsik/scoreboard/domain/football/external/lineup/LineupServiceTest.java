package com.gyechunsik.scoreboard.domain.football.external.lineup;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartLineup;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartPlayer;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.StartLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.StartPlayerRepository;
import com.gyechunsik.scoreboard.domain.football.util.GenerateLeagueTeamFixture;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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
    private StartLineupRepository startLineupRepository;
    @Autowired
    private StartPlayerRepository startPlayerRepository;

    @Transactional
    @DisplayName("singleFixture 응답으로 StartLineup 과 StartPlayer 저장 성공")
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
        List<StartLineup> all = startLineupRepository.findAll();
        Fixture fixture = fixtureRepository.findById(FixtureId.FIXTURE_SINGLE_1145526).orElseThrow();
        StartLineup homeStartLineup = startLineupRepository.findByFixtureAndTeam(fixture, fixture.getHomeTeam()).orElseThrow();
        StartLineup awayStartLineup = startLineupRepository.findByFixtureAndTeam(fixture, fixture.getAwayTeam()).orElseThrow();
        log.info("homeStartLineup :: {}", homeStartLineup);

        List<StartPlayer> homeLineupPlayers = startPlayerRepository.findByStartLineup(homeStartLineup);
        homeLineupPlayers.forEach(startPlayer -> {
            log.info("startPlayer :: (id={},name={},isSub={})",
                    startPlayer.getPlayer().getId(),
                    startPlayer.getPlayer().getName(),
                    startPlayer.getSubstitute()
            );
        });
        List<StartPlayer> awayLineupPlayers = startPlayerRepository.findByStartLineup(awayStartLineup);
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