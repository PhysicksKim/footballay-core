package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.available.entity.AvailableLeague;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.LeagueTeamRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.gyechunsik.scoreboard.domain.football.util.GenerateLeagueTeamFixture.*;
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
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EntityManager em;

    @Disabled
    @Test
    @DisplayName("리그를 ID로 캐싱합니다")
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

    @Disabled
    @Test
    @DisplayName("리그에 속한 모든 팀을 캐싱합니다")
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

    @Disabled
    @Test
    @DisplayName("팀의 선수 명단을 캐싱합니다")
    void successCacheSquadOfATeamByTeamId() {
        // given
        final Long leagueId = LeagueId.EURO;
        final Long teamId = TeamId.PORTUGAL;
        footballRoot.cacheLeagueById(leagueId);
        footballRoot.cacheTeamsOfLeague(leagueId);

        // when
        boolean isSuccess = footballRoot.cacheSquadOfTeam(teamId);

        // then
        playerRepository.findAllByTeam(teamId).forEach(player -> {
            log.info("Player : {}", player);
            assertThat(player.getName()).isNotNull();
            player.getTeamPlayers().forEach(teamPlayer -> assertThat(teamPlayer.getTeam().getName()).isNotNull());
        });
        assertThat(isSuccess).isTrue();
    }

    @Disabled
    @Test
    @DisplayName("리그의 현재 시즌의 모든 경기 일정을 캐싱합니다")
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

    @Test
    @DisplayName("캐싱된 리그 목록을 가져옵니다")
    void successGetLeagues() {
        // given
        List<LeagueTeamFixture> list = generateTwoOtherLeagues();
        League league1 = list.get(0).league;
        League league2 = list.get(1).league;
        leagueRepository.save(league1);
        leagueRepository.save(league2);

        // when
        List<League> leagues = footballRoot.getLeagues();

        // then
        assertThat(leagues).isNotEmpty();
        assertThat(leagues).hasSize(2);
        leagues.forEach(league -> {
            assertThat(league.getName()).isNotNull();
            assertThat(league.getLeagueId()).isNotNull();
        });
    }

    @Test
    @DisplayName("캐싱된 리그의 팀 목록을 가져옵니다")
    void successGetTeamsByLeagueId() {
        // given
        final Long leagueId = LeagueId.EURO;
        footballRoot.cacheLeagueById(leagueId);
        footballRoot.cacheTeamsOfLeague(leagueId);

        em.flush();
        em.clear();

        // when
        List<Team> teams = footballRoot.getTeamsByLeagueId(leagueId);

        // then
        assertThat(teams).isNotEmpty();
        teams.forEach(team -> {
            assertThat(team.getName()).isNotNull();
            assertThat(team.getLeagueTeams()).isNotEmpty();
            team.getLeagueTeams().forEach(
                    leagueTeam -> assertThat(leagueTeam.getLeague().getName()).isNotNull()
            );
        });
    }

    @Test
    @DisplayName("캐싱된 팀의 선수 목록을 가져옵니다")
    void successGetSquadsByTeamId() {
        // given
        final Long leagueId = LeagueId.EURO;
        final Long teamId = TeamId.PORTUGAL;
        footballRoot.cacheLeagueById(leagueId);
        footballRoot.cacheTeamsOfLeague(leagueId);
        footballRoot.cacheSquadOfTeam(teamId);

        em.flush();
        em.clear();

        // when
        List<Player> players = footballRoot.getSquadByTeamId(teamId);

        // then
        assertThat(players).isNotEmpty();
        players.forEach(player -> {
            assertThat(player.getName()).isNotNull();
            assertThat(player.getTeamPlayers()).isNotEmpty();
            player.getTeamPlayers().forEach(
                    teamPlayer -> assertThat(teamPlayer.getTeam().getName()).isNotNull()
            );
        });
    }

    @Test
    @DisplayName("캐싱된 리그의 경기 일정을 가져옵니다")
    void successGetFixturesByLeagueId() {
        // given
        final Long leagueId = LeagueId.EURO;
        footballRoot.cacheLeagueById(leagueId);
        footballRoot.cacheTeamsOfLeague(leagueId);
        footballRoot.cacheAllFixturesOfLeague(leagueId);

        ZonedDateTime beforeEuro2024Start = ZonedDateTime.parse("2024-06-01T00:00:00Z");

        // when
        List<Fixture> nextFixturesFromDate = footballRoot.getNextFixturesFromDate(leagueId, beforeEuro2024Start);

        // then
        assertThat(nextFixturesFromDate).isNotEmpty();
        nextFixturesFromDate.forEach(fixture -> {
            assertThat(fixture.getHomeTeam().getName()).isNotNull();
            assertThat(fixture.getAwayTeam().getName()).isNotNull();
        });
    }

    @Test
    @DisplayName("리그를 즐겨찾기에 추가하고 조회합니다")
    void successAddFavoriteLeagueByLeagueId() {
        // given
        final Long leagueId = LeagueId.EURO;
        footballRoot.cacheLeagueById(leagueId);

        // when
        footballRoot.addFavoriteLeague(leagueId);

        // then
        List<AvailableLeague> availableLeagues = footballRoot.getFavoriteLeagues();
        assertThat(availableLeagues).isNotEmpty();
        assertThat(availableLeagues).hasSize(1);
        assertThat(availableLeagues.get(0).getLeagueId()).isEqualTo(leagueId);
    }

    @Test
    @DisplayName("리그를 즐겨찾기에서 삭제합니다")
    void successRemoveFavoriteLeagueByLeagueId() {
        // given
        final Long leagueId = LeagueId.EURO;
        footballRoot.cacheLeagueById(leagueId);
        footballRoot.addFavoriteLeague(leagueId);

        // when
        footballRoot.removeFavoriteLeague(leagueId);

        // then
        List<AvailableLeague> availableLeagues = footballRoot.getFavoriteLeagues();
        assertThat(availableLeagues).isEmpty();
    }

    @Test
    @DisplayName("이용 가능한 경기 일정을 추가합니다")
    void successAddAvailableFixture() {
        // given


    }

    @Test
    @DisplayName("이용 가능한 경기 일정을 조회합니다")
    void successGetAvailableFixtures() {

    }

    @Test
    @DisplayName("이용 가능한 경기 일정을 삭제합니다")
    void successRemoveAvailableFixture() {

    }


}