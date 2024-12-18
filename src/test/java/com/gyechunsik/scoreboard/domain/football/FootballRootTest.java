package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.dto.*;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.LiveStatus;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.LiveStatusRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.LeagueTeamRepository;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.PreviousMatchProcessor;
import com.gyechunsik.scoreboard.domain.football.scheduler.live.LiveMatchProcessor;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.gyechunsik.scoreboard.domain.football.util.GenerateLeagueTeamFixture.*;
import static com.gyechunsik.scoreboard.util.TestJobKeyUtil.createLiveMatchJobKey;
import static com.gyechunsik.scoreboard.util.TestJobKeyUtil.createPreviousMatchJobKey;
import static com.gyechunsik.scoreboard.util.TestQuartzJobWaitUtil.waitForJobToBeRemoved;
import static com.gyechunsik.scoreboard.util.TestQuartzJobWaitUtil.waitForJobToBeScheduled;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional
@ActiveProfiles({"dev", "mockapi"})
class FootballRootTest {

    @Autowired
    private FootballRoot footballRoot;

    @Autowired
    private Scheduler scheduler;

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
    private PreviousMatchProcessor startLineupProcessor;
    @Autowired
    private LiveMatchProcessor liveMatchProcessor;
    @Autowired
    private LiveStatusRepository liveStatusRepository;

    @AfterEach
    protected void cleanJob() throws SchedulerException {
        scheduler.clear();
    }

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
        List<LeagueDto> leagues = footballRoot.getLeagues();

        // then
        assertThat(leagues).isNotEmpty();
        assertThat(leagues).hasSize(2);
        leagues.forEach(league -> {
            assertThat(league.name()).isNotNull();
            assertThat(league.leagueId()).isNotNull();
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
        List<TeamDto> teams = footballRoot.getTeamsOfLeague(leagueId);

        // then
        assertThat(teams).isNotEmpty();
        teams.forEach(team -> {
            assertThat(team.name()).isNotNull();
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
        List<PlayerDto> players = footballRoot.getSquadOfTeam(teamId);

        // then
        assertThat(players).isNotEmpty();
        players.forEach(player -> {
            assertThat(player.name()).isNotNull();
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
        List<FixtureInfoDto> nextFixturesFromDate = footballRoot.getNextFixturesFromDate(leagueId, beforeEuro2024Start);

        // then
        assertThat(nextFixturesFromDate).isNotEmpty();
        nextFixturesFromDate.forEach(fixture -> {
            assertThat(fixture.homeTeam().name()).isNotNull();
            assertThat(fixture.awayTeam().name()).isNotNull();
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
        List<LeagueDto> availableLeagues = footballRoot.getAvailableLeagues();
        assertThat(availableLeagues).isNotEmpty();
        assertThat(availableLeagues).hasSize(1);
        assertThat(availableLeagues.get(0).leagueId()).isEqualTo(leagueId);
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
        List<LeagueDto> availableLeagues = footballRoot.getAvailableLeagues();
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
        LiveStatus saveLiveStatus = liveStatusRepository.save(createFullTimeLiveStatus());
        fixture.setLiveStatus(saveLiveStatus);
        fixtureRepository.save(fixture);

        em.flush();
        em.clear();

        // when
        long fixtureId = fixture.getFixtureId();
        FixtureInfoDto addAvailableFixture = footballRoot.addAvailableFixture(fixtureId);

        // then
        assertThat(addAvailableFixture.available()).isTrue();
    }

    private static LiveStatus createFullTimeLiveStatus() {
        return LiveStatus.builder()
                .elapsed(90)
                .longStatus("Match Finished")
                .shortStatus("FT")
                .homeScore(1)
                .awayScore(0)
                .build();
    }

    @Test
    @DisplayName("이용 가능한 경기 일정을 조회합니다")
    void successGetAvailableFixturesOnClosestDate() {
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
        LiveStatus liveStatus = liveStatusRepository.save(createNotStartedLiveStatus());
        fixture.setLiveStatus(liveStatus);
        fixtureRepository.save(fixture);

        em.flush();
        em.clear();

        // when
        long fixtureId = fixture.getFixtureId();
        FixtureInfoDto addAvailableFixture = footballRoot.addAvailableFixture(fixtureId);

        List<FixtureInfoDto> availableFixtures = footballRoot.getAvailableFixturesOnClosestDate(
                league.getLeagueId(),
                ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()));
        log.info("available fixtures = {}", availableFixtures);

        // then
        assertThat(addAvailableFixture.available()).isTrue();
        assertThat(availableFixtures).isNotEmpty();
        assertThat(availableFixtures).hasSize(1);
        assertThat(availableFixtures.get(0).fixtureId()).isEqualTo(fixture.getFixtureId());
    }

    private static LiveStatus createNotStartedLiveStatus() {
        return LiveStatus.builder()
                .longStatus("Not started")
                .shortStatus("NS")
                .elapsed(0)
                .build();
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
        LiveStatus liveStatus = liveStatusRepository.save(createNotStartedLiveStatus());
        fixture.setLiveStatus(liveStatus);
        fixtureRepository.save(fixture);

        em.flush();
        em.clear();

        long fixtureId = fixture.getFixtureId();
        FixtureInfoDto addAvailableFixture = footballRoot.addAvailableFixture(fixtureId);

        em.flush();
        em.clear();

        // when
        boolean isSuccess = footballRoot.removeAvailableFixture(fixtureId);
        waitUntilJobRemoved(fixtureId);

        // then
        assertThat(addAvailableFixture.available()).isTrue();
        assertThat(isSuccess).isTrue();
        fixtureRepository.findById(fixture.getFixtureId()).ifPresent(f -> {
            assertThat(f.isAvailable()).isFalse();
        });
    }

    private void waitUntilJobRemoved(long fixtureId) {
        JobKey previousMatchJobKey = createPreviousMatchJobKey(fixtureId);
        JobKey liveMatchJobKey = createLiveMatchJobKey(fixtureId);

        waitForJobToBeRemoved(scheduler, previousMatchJobKey);
        waitForJobToBeRemoved(scheduler, liveMatchJobKey);
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
        liveMatchProcessor.requestAndSaveLiveMatchData(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);

        em.flush();
        em.clear();

        // when
        FixtureWithLineupDto eagerFixture = footballRoot.getFixtureWithLineup(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));

        // then
        assertThat(eagerFixture.homeLineup().team().name()).isNotNull();
        assertThat(eagerFixture.awayLineup().team().name()).isNotNull();
        assertThat(eagerFixture.fixture()).isNotNull();
        assertThat(eagerFixture.fixture().liveStatus()).isNotNull();
    }

    private void waitUntilJobAdded(long fixtureId) {
        JobKey previousMatchJobKey = createPreviousMatchJobKey(fixtureId);
        JobKey liveMatchJobKey = createLiveMatchJobKey(fixtureId);

        waitForJobToBeScheduled(scheduler, liveMatchJobKey);
        waitForJobToBeScheduled(scheduler, previousMatchJobKey);
    }

}