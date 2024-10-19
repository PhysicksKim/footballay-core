package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.PlayerStatistics;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.MockApiCallServiceImpl;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.PlayerStatisticsRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.TeamStatisticsRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
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
import static org.junit.jupiter.api.Assertions.*;

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
    private TeamStatisticsRepository teamStatisticsRepository;
    @Autowired
    private PlayerStatisticsRepository playerStatisticsRepository;

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

        // when
        playerStatisticsService.savePlayerStatistics(response);
        em.flush();
        em.clear();

        // then
        Fixture fixture = getFixture();
        List<PlayerStatistics> homePlayerStatisticsList = getPlayerStatistics(fixture,true);
        List<PlayerStatistics> awayPlayerStatisticsList = getPlayerStatistics(fixture,false);

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
        List<PlayerStatistics> homePlayerStatisticsList = getPlayerStatistics(fixture, true);

        // 수정된 득점 수가 반영되었는지 확인
        homePlayerStatisticsList.forEach(ps -> {
            assertThat(ps.getGoals()).isEqualTo(10);
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

    @DisplayName("새로운 선수 정보가 있을 때 캐싱 및 저장")
    @Test
    void save_withNewPlayer_shouldCacheAndSave() {
        // given
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);

        List<_PlayerStatistics> playerStatisticsList = response.getResponse().get(0).getPlayers().get(0).getPlayers();

        // 새로운 선수로 수정
        _PlayerStatistics newPlayerStatistics = playerStatisticsList.get(0);
        _Player newPlayer = new _Player();
        newPlayer.setId(9999999L);
        newPlayer.setName("New Player");
        newPlayer.setPhoto("http://example.com/photo.jpg");
        newPlayerStatistics.setPlayer(newPlayer);

        _Statistics statistics = newPlayerStatistics.getStatistics().get(0);
        statistics.getGames().setMinutes(90);
        statistics.getGames().setPosition("Forward");
        statistics.getGames().setRating("7.5");
        statistics.getGames().setCaptain(false);
        statistics.getGames().setSubstitute(false);
        statistics.getGoals().setTotal(1);

        // when
        playerStatisticsService.savePlayerStatistics(response);
        em.flush();
        em.clear();

        // then
        Fixture fixture = getFixture();
        List<PlayerStatistics> homePlayerStatisticsList = getPlayerStatistics(fixture, true);

        // 새로운 선수가 저장되었는지 확인
        boolean newPlayerSaved = homePlayerStatisticsList.stream()
                .anyMatch(ps -> ps.getPlayer().getId().equals(9999999L));

        assertThat(newPlayerSaved).isTrue();
    }

    private List<PlayerStatistics> getPlayerStatistics(Fixture fixture, boolean isHome) {
        Team team = isHome ? fixture.getHomeTeam() : fixture.getAwayTeam();
        List<PlayerStatistics> playerStatisticsList = playerStatisticsRepository.findByFixtureAndTeam(fixture, team);
        return playerStatisticsList;
    }

}