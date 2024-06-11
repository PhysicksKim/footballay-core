package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.LeagueTeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
@ActiveProfiles("mockapi")
class FootballRootTest {

    @Autowired
    private FootballRoot footballRoot;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;
    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private FixtureRepository fixtureRepository;

    /*
    1) cache league by id
    2) cache all teams of league by league id
    3) cache squad of a team by team id
    4) cache all fixtures of current season of a league by league id
     */
    @Test
    void successCacheLeagueById() {
        // given
        final Long leagueId = LeagueId.EURO;

        // when
        boolean isSuccess = footballRoot.cacheLeagueById(leagueId);

        // then
        Optional<League> leagueOptional = leagueRepository.findById(leagueId);
        assertThat(isSuccess).isTrue();
        assertThat(leagueOptional).isPresent();
        assertThat(leagueOptional.get().getName()).isNotNull();
        assertThat(leagueOptional.get().getLeagueId()).isEqualTo(leagueId);
    }

    @Test
    void successAllTeamsOfLeagueByLeagueId() {
        // given
        final Long leagueId = LeagueId.EURO;
        footballRoot.cacheLeagueById(leagueId);

        // when
        boolean isSuccess = footballRoot.cacheTeamsOfLeague(leagueId);

        // then
        List<LeagueTeam> all = leagueTeamRepository.findAll();
        log.info("All League Team Relations : League={}, teams=[{}]",
                all.get(0).getLeague().getName(),
                all.stream().map(lt -> lt.getTeam().getName()).toList());

        assertThat(isSuccess).isTrue();
        assertThat(all).isNotEmpty();
        assertThat(all.get(0).getLeague().getName()).isNotNull();
        assertThat(all.get(0).getTeam().getName()).isNotNull();
    }

    @Test
    void successCacheSquadOfATeamByTeamId() {
        // given
        final Long leagueId = LeagueId.EURO;
        final Long teamId = TeamId.PORTUGAL;
        footballRoot.cacheLeagueById(leagueId);
        footballRoot.cacheTeamsOfLeague(leagueId);

        // when
        boolean isSuccess = footballRoot.cacheSquadOfTeam(teamId);

        // then
        playerRepository.findAllByTeamId(teamId).forEach(player -> {
            log.info("Player : {}", player);
            assertThat(player.getName()).isNotNull();
            assertThat(player.getTeam().getName()).isNotNull();
        });
        assertThat(isSuccess).isTrue();
    }

    @Test
    void successCacheAllFixturesOfCurrentSeasonOfALeagueByLeagueId() {
        // given
        final Long leagueId = LeagueId.EURO;
        footballRoot.cacheLeagueById(leagueId);
        footballRoot.cacheTeamsOfLeague(leagueId);

        // when
        boolean isSuccess = footballRoot.cacheAllFixturesOfLeague(leagueId);

        // then
        Optional<League> leagueOptional = leagueRepository.findById(leagueId);
        League league = leagueOptional.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리그입니다."));
        fixtureRepository.findAllByLeague(league).forEach(fixture -> {
            assertThat(fixture.getHomeTeam().getName()).isNotNull();
            assertThat(fixture.getAwayTeam().getName()).isNotNull();
        });
        assertThat(isSuccess).isTrue();
    }
}