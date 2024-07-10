package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.LeagueTeamRepository;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.StartLineupProcessor;
import com.gyechunsik.scoreboard.domain.football.scheduler.live.LiveFixtureProcessor;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
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
    private EntityManager em;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private FootballApiCacheService footballApiCacheService;
    @Autowired
    private StartLineupProcessor startLineupProcessor;
    @Autowired
    private LiveFixtureProcessor liveFixtureProcessor;

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
        log.info("All _League _Team Relations : _League={}, teams=[{}]",
                all.get(0).getLeague().getName(),
                all.stream().map(lt -> lt.getTeam().getName()).toList());

        assertThat(isSuccess).isTrue();
        assertThat(all).isNotEmpty();
        assertThat(all.get(0).getLeague().getName()).isNotNull();
        assertThat(all.get(0).getTeam().getName()).isNotNull();
    }

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

        em.flush();
        em.clear();

        // then
        playerRepository.findAllByTeam(teamId).forEach(player -> {
            log.info("_Player : {}", player);
            assertThat(player.getName()).isNotNull();
            player.getTeamPlayers().forEach(teamPlayer -> assertThat(teamPlayer.getTeam().getName()).isNotNull());
        });
        assertThat(isSuccess).isTrue();
    }

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
    void successGetTeamsOfLeague() {
        // given
        final Long leagueId = LeagueId.EURO;
        footballRoot.cacheLeagueById(leagueId);
        footballRoot.cacheTeamsOfLeague(leagueId);

        // leagueTeams 연관관계 collection 을 채우기 위해 1차 캐시 flush clear 해야합니다
        em.flush();
        em.clear();

        // when
        List<Team> teams = footballRoot.getTeamsOfLeague(leagueId);

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

        // leagueTeams 연관관계 collection 을 채우기 위해 1차 캐시 flush clear 해야합니다
        em.flush();
        em.clear();

        // when
        List<Player> players = footballRoot.getSquadOfTeam(teamId);

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
    void successAddAvailableLeagueByLeagueId() {
        // given
        final Long leagueId = LeagueId.EURO;
        footballRoot.cacheLeagueById(leagueId);

        // when
        footballRoot.addAvailableLeague(leagueId);

        // then
        List<League> availableLeagues = footballRoot.getAvailableLeagues();
        assertThat(availableLeagues).isNotEmpty();
        assertThat(availableLeagues).hasSize(1);
        assertThat(availableLeagues.get(0).getLeagueId()).isEqualTo(leagueId);
    }

    @Test
    @DisplayName("리그를 즐겨찾기에서 삭제합니다")
    void successRemoveAvailableLeagueByLeagueId() {
        // given
        final Long leagueId = LeagueId.EURO;
        footballRoot.cacheLeagueById(leagueId);
        footballRoot.addAvailableLeague(leagueId);

        // when
        footballRoot.removeAvailableLeague(leagueId);

        // then
        List<League> availableLeagues = footballRoot.getAvailableLeagues();
        assertThat(availableLeagues).isEmpty();
    }

    @Test
    @DisplayName("이용 가능한 경기 일정을 추가합니다")
    void successAddAvailableFixture() {
        // given
        LeagueTeamFixture leagueTeamFixture = generate();
        League league = leagueTeamFixture.league;
        Team homeTeam = leagueTeamFixture.home;
        Team awayTeam = leagueTeamFixture.away;
        Fixture fixture = leagueTeamFixture.fixture;
        leagueRepository.save(league);
        teamRepository.saveAll(List.of(homeTeam, awayTeam));
        LeagueTeam leagueTeamHome = LeagueTeam.builder().league(league).team(homeTeam).build();
        LeagueTeam leagueTeamAway = LeagueTeam.builder().league(league).team(awayTeam).build();
        leagueTeamRepository.saveAll(List.of(leagueTeamHome, leagueTeamAway));
        fixtureRepository.save(fixture);

        em.flush();
        em.clear();

        // when
        Fixture addAvailableFixture = footballRoot.addAvailableFixture(fixture.getFixtureId());

        // then
        assertThat(addAvailableFixture.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("이용 가능한 경기 일정을 조회합니다")
    void successGetAvailableFixtures() {
        // given
        LeagueTeamFixture leagueTeamFixture = generate();
        League league = leagueTeamFixture.league;
        Team homeTeam = leagueTeamFixture.home;
        Team awayTeam = leagueTeamFixture.away;
        Fixture fixture = leagueTeamFixture.fixture;
        leagueRepository.save(league);
        teamRepository.saveAll(List.of(homeTeam, awayTeam));
        LeagueTeam leagueTeamHome = LeagueTeam.builder().league(league).team(homeTeam).build();
        LeagueTeam leagueTeamAway = LeagueTeam.builder().league(league).team(awayTeam).build();
        leagueTeamRepository.saveAll(List.of(leagueTeamHome, leagueTeamAway));
        fixtureRepository.save(fixture);

        em.flush();
        em.clear();

        // when
        Fixture addAvailableFixture = footballRoot.addAvailableFixture(fixture.getFixtureId());
        List<Fixture> availableFixtures = footballRoot.getAvailableFixtures(
                league.getLeagueId(),
                ZonedDateTime.of(2023,1,1,0,0,0,0, ZoneId.systemDefault()));

        // then
        assertThat(addAvailableFixture.isAvailable()).isTrue();
        assertThat(availableFixtures).isNotEmpty();
        assertThat(availableFixtures).hasSize(1);
        assertThat(availableFixtures.get(0).getFixtureId()).isEqualTo(fixture.getFixtureId());
    }

    @Test
    @DisplayName("이용 가능한 경기 일정을 삭제합니다")
    void successRemoveAvailableFixture() {
        // given
        LeagueTeamFixture leagueTeamFixture = generate();
        League league = leagueTeamFixture.league;
        Team homeTeam = leagueTeamFixture.home;
        Team awayTeam = leagueTeamFixture.away;
        Fixture fixture = leagueTeamFixture.fixture;
        leagueRepository.save(league);
        teamRepository.saveAll(List.of(homeTeam, awayTeam));
        LeagueTeam leagueTeamHome = LeagueTeam.builder().league(league).team(homeTeam).build();
        LeagueTeam leagueTeamAway = LeagueTeam.builder().league(league).team(awayTeam).build();
        leagueTeamRepository.saveAll(List.of(leagueTeamHome, leagueTeamAway));
        fixtureRepository.save(fixture);

        em.flush();
        em.clear();

        Fixture addAvailableFixture = footballRoot.addAvailableFixture(fixture.getFixtureId());

        em.flush();
        em.clear();

        // when
        boolean isSuccess = footballRoot.removeAvailableFixture(fixture.getFixtureId());

        // then
        assertThat(addAvailableFixture.isAvailable()).isTrue();
        assertThat(isSuccess).isTrue();
        fixtureRepository.findById(fixture.getFixtureId()).ifPresent(f -> {
            assertThat(f.isAvailable()).isFalse();
        });
    }

    @DisplayName("라이브 정보를 포함한 경기 정보를 모두 채워서 가져오는 조회에 성공합니다")
    @Test
    void getFixtureInfos() {
        // given
        footballApiCacheService.cacheLeague(LeagueId.EURO);
        footballApiCacheService.cacheTeamsOfLeague(LeagueId.EURO);
        footballApiCacheService.cacheTeamSquad(TeamId.SPAIN);
        footballApiCacheService.cacheTeamSquad(TeamId.CROATIA);
        footballApiCacheService.cacheFixturesOfLeague(LeagueId.EURO);
        startLineupProcessor.requestAndSaveLineup(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);
        liveFixtureProcessor.requestAndSaveLiveFixtureData(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);

        em.flush();
        em.clear();

        // when
        Fixture eagerFixture = footballRoot.getFixtureWithEager(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));

        em.detach(eagerFixture);

        // then
        assertThat(eagerFixture.getHomeTeam().getName()).isNotNull();
        assertThat(eagerFixture.getAwayTeam().getName()).isNotNull();
        assertThat(eagerFixture.getLiveStatus()).isNotNull();
        assertThat(eagerFixture.getLineups()).isNotNull().isNotEmpty();
        assertThat(eagerFixture.getEvents()).isNotNull().isNotEmpty();

    }

}