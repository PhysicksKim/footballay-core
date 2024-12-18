package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.MockApiCallServiceImpl;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.lineup.LineupService;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse._FixturePlayers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@Transactional
@ActiveProfiles({"dev", "mockapi"})
@SpringBootTest
class PlayerStatisticsServiceTest {

    @Autowired
    private PlayerStatisticsService playerStatisticsService;

    @Autowired
    private FootballApiCacheService footballApiCacheService;

    @Autowired
    private FixtureRepository fixtureRepository;

    @Autowired
    private EntityManager em;

    private ApiCallService apiCallService;

    private static final long FIXTURE_ID = FixtureId.FIXTURE_SINGLE_1145526;

    @Autowired
    private MatchPlayerRepository matchPlayerRepository;
    @Autowired
    private LineupService lineupService;

    @BeforeEach
    public void setup() {
        apiCallService = new MockApiCallServiceImpl();
        cacheFootballData();
        em.clear();
    }

    private void cacheFootballData() {
        footballApiCacheService.cacheLeague(4L);
        footballApiCacheService.cacheTeamsOfLeague(4L);
        footballApiCacheService.cacheTeamSquad(777);
        footballApiCacheService.cacheTeamSquad(27);
        footballApiCacheService.cacheFixturesOfLeague(4L);
    }

    private Fixture getFixture() {
        return fixtureRepository.findById(FIXTURE_ID).orElseThrow();
    }

    @DisplayName("선수 통계 저장 성공")
    @Test
    void save() {
        // given
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);
        lineupService.saveLineup(response);
        em.flush();
        em.clear();

        // when
        playerStatisticsService.savePlayerStatistics(response);
        em.flush();
        em.clear();

        // then
        Fixture fixture = getFixture();
        List<MatchPlayer> homePlayerStatisticsList = getPlayerStatistics(fixture,true);
        List<MatchPlayer> awayPlayerStatisticsList = getPlayerStatistics(fixture,false);

        assertThat(homePlayerStatisticsList.size()).isGreaterThan(0);
        assertThat(awayPlayerStatisticsList.size()).isGreaterThan(0);
    }

    @DisplayName("존재하지 않는 경기 정보 저장 시 예외 발생")
    @Test
    void save_withNonExistentFixture_shouldThrowException() {
        // given
        long nonExistentFixtureId = 9999999L; // 존재하지 않는 fixtureId
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);
        response.getResponse().get(0).getFixture().setId(nonExistentFixtureId);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            playerStatisticsService.savePlayerStatistics(response);
        });
    }

    @DisplayName("이미 저장된 선수 통계 정보 업데이트")
    @Test
    void save_whenPlayerStatisticsAlreadyExists_shouldUpdate() {
        // given
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);

        // 최초 저장
        playerStatisticsService.savePlayerStatistics(response);
        em.flush();
        em.clear();

        // 선수 통계 정보를 수정하여 업데이트
        // 예를 들어 첫 번째 선수의 득점 수를 증가시킵니다.
        response.getResponse().get(0).getPlayers().forEach(teamPlayers -> {
            teamPlayers.getPlayers().forEach(playerStat -> {
                playerStat.getStatistics().get(0).getGoals().setTotal(10);
            });
        });

        // when
        playerStatisticsService.savePlayerStatistics(response);
        em.flush();
        em.clear();

        // then
        Fixture fixture = getFixture();
        List<MatchPlayer> homePlayerStatisticsList = getPlayerStatistics(fixture, true);

        // 수정된 득점 수가 반영되었는지 확인
        homePlayerStatisticsList.forEach(mp -> {
            assertThat(mp.getPlayerStatistics().getGoals()).isEqualTo(10);
        });
    }

    @DisplayName("선수 통계 정보가 없더라도 예외 발생하지 않음")
    @Test
    void save_withNoPlayerStatistics_shouldThrowException() {
        // given
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);

        // 선수 통계 정보를 빈 리스트로 설정합니다.
        response.getResponse().get(0).setPlayers(new ArrayList<>());

        // when & then
        assertDoesNotThrow(() -> {
            playerStatisticsService.savePlayerStatistics(response);
        });
    }

    @DisplayName("라인업에 없는 미등록 선수가 통계에만 등장할 경우, 해당 통계는 무시되어야 한다.")
    @Test
    void saveStats_UnregisteredPlayerNotInLineup_ShouldIgnoreStats() {
        // given
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);

        // 라인업 저장
        lineupService.saveLineup(response);
        em.flush();
        em.clear();

        // 통계 정보에서 라인업에 없는 미등록 선수 추가
        // 실제 로직 상 이 선수는 matchPlayerMap 에 없으므로 처리되지 않아야 함
        List<FixtureSingleResponse._FixturePlayers> playersData = response.getResponse().get(0).getPlayers();
        if (!playersData.isEmpty()) {
            FixtureSingleResponse._FixturePlayers teamPlayers = playersData.get(0);
            List<_PlayerStatistics> playerStatsList = teamPlayers.getPlayers();

            // 라인업에 존재하지 않는 미등록 선수 추가
            _PlayerStatistics newUnregiPlayerStat = new _PlayerStatistics();
            _Player newPlayer = new _Player();
            newPlayer.setId(null); // 미등록
            newPlayer.setName("No Lineup Unregistered Player Only In Stats");
            newPlayer.setPhoto("http://example.com/unregi_player_photo.png");

            // 기본 스탯 초기화
            _Statistics statistics = new _Statistics();
            statistics.setGames(new _Statistics._Games());
            statistics.setShots(new _Statistics._Shots());
            statistics.setGoals(new _Statistics._Goals());
            statistics.setPasses(new _Statistics._Passes());
            statistics.setTackles(new _Statistics._Tackles());
            statistics.setDuels(new _Statistics._Duels());
            statistics.setDribbles(new _Statistics._Dribbles());
            statistics.setFouls(new _Statistics._Fouls());
            statistics.setCards(new _Statistics._Cards());
            statistics.setPenalty(new _Statistics._Penalty());
            statistics.getGames().setMinutes(90); // 임의값

            newUnregiPlayerStat.setPlayer(newPlayer);
            newUnregiPlayerStat.setStatistics(List.of(statistics));

            playerStatsList.add(newUnregiPlayerStat);
        }

        // when
        playerStatisticsService.savePlayerStatistics(response);
        em.flush();
        em.clear();

        // then
        Fixture fixture = fixtureRepository.findById(FIXTURE_ID).orElseThrow();
        // 해당 선수가 MatchPlayer 로 저장되어 있지 않아야 함 (즉, 통계 무시)
        List<MatchLineup> lineups = fixture.getLineups();
        List<MatchPlayer> matchPlayers1 = lineups.get(0).getMatchPlayers();
        List<MatchPlayer> matchPlayers2 = lineups.get(1).getMatchPlayers();

        List<MatchPlayer> matchPlayers = new ArrayList<>();
        matchPlayers.addAll(matchPlayers1);
        matchPlayers.addAll(matchPlayers2);
        boolean containsNoLineupUnregiPlayer = matchPlayers.stream()
                .anyMatch(mp -> "No Lineup Unregistered Player Only In Stats".equals(mp.getUnregisteredPlayerName()));
        assertThat(containsNoLineupUnregiPlayer).isFalse();

        // PlayerStatistics 테이블에도 해당 선수의 통계가 생성되지 않았는지 확인
        // 저장된 matchPlayers 들 중 PlayerStatistics가 생성된 경우만 확인, 새로운 이름의 unregistered 선수 없어야 함
        boolean invalidStatsCreated = matchPlayers.stream()
                .filter(mp -> mp.getPlayerStatistics() != null)
                .anyMatch(mp -> "No Lineup Unregistered Player Only In Stats".equals(mp.getUnregisteredPlayerName()));
        assertThat(invalidStatsCreated).isFalse();
    }

    /**
     * 예상치 못하게 통계에만 새롭게 id 가 존재하는 등록선수가 추가되는 경우
     * @param playerStatisticsList
     */
    private static void addUnexpectedNewRegisteredPlayerOnlyInStatistics(List<_PlayerStatistics> playerStatisticsList) {
        _PlayerStatistics newPlayerStatistics = new _PlayerStatistics();

        _Player newPlayer = new _Player();
        newPlayer.setId(9999999L);
        newPlayer.setName("New Player");
        newPlayer.setPhoto("http://example.com/photo.jpg");

        _Statistics statistics = new _Statistics();
        statistics.setGames(new _Statistics._Games());
        statistics.setShots(new _Statistics._Shots());
        statistics.setGoals(new _Statistics._Goals());
        statistics.setPasses(new _Statistics._Passes());
        statistics.setTackles(new _Statistics._Tackles());
        statistics.setDuels(new _Statistics._Duels());
        statistics.setDribbles(new _Statistics._Dribbles());
        statistics.setFouls(new _Statistics._Fouls());
        statistics.setCards(new _Statistics._Cards());
        statistics.setPenalty(new _Statistics._Penalty());
        statistics.getGames().setMinutes(90);
        statistics.getGames().setPosition("Forward");
        statistics.getGames().setRating("7.5");
        statistics.getGames().setCaptain(false);
        statistics.getGames().setSubstitute(false);
        statistics.getGoals().setTotal(1);

        newPlayerStatistics.setPlayer(newPlayer);
        newPlayerStatistics.setStatistics(List.of(statistics));
        playerStatisticsList.add(newPlayerStatistics);
    }

    private List<MatchPlayer> getPlayerStatistics(Fixture fixture, boolean isHome) {
        Team team = isHome ? fixture.getHomeTeam() : fixture.getAwayTeam();

        return matchPlayerRepository.findMatchPlayerByFixtureAndTeam(fixture, team);
    }

}