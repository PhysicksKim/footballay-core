package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.live.TeamStatistics;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.MockApiCallServiceImpl;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.TeamStatisticsRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@Transactional
@ActiveProfiles({"dev", "mockapi"})
@SpringBootTest
class TeamStatisticsServiceTest {

    @Autowired
    private TeamStatisticsService teamStatisticsService;
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

    private TeamStatistics getTeamStatistics(Fixture fixture, boolean isHomeTeam) {
        return teamStatisticsRepository.findByFixtureAndTeam(
                fixture, isHomeTeam ? fixture.getHomeTeam() : fixture.getAwayTeam()
        ).orElseThrow();
    }

    private void logXgValues(TeamStatistics teamStatistics, String teamType) {
        log.info("{} team Statistics={}", teamType, teamStatistics);
        log.info("{} xg values = {}", teamType, teamStatistics.getXgString());
    }

    @DisplayName("팀 통계 저장 성공")
    @Test
    void save() {
        // when
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);
        teamStatisticsService.saveTeamStatistics(response);
        em.flush();
        em.clear();

        // then
        Fixture fixture = getFixture();
        TeamStatistics homeTeamStatistics = getTeamStatistics(fixture, true);
        TeamStatistics awayTeamStatistics = getTeamStatistics(fixture, false);

        assertThat(homeTeamStatistics).isNotNull();
        assertThat(homeTeamStatistics.getShotsOnGoal()).isNotNull();
        assertThat(homeTeamStatistics.getShotsOffGoal()).isNotNull();
        assertThat(homeTeamStatistics.getTotalShots()).isNotNull();

        logXgValues(homeTeamStatistics, "home");
        logXgValues(awayTeamStatistics, "away");
    }

    @DisplayName("xg 값 업데이트 - elapsed 같으면 xg 값 업데이트")
    @Test
    void updateXgValueWhenElapsedSame() {
        // given
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);
        log.info("does original? xg = {}", response.getResponse().get(0).getStatistics().get(0).getStatistics().stream()
                .filter(stat -> "expected_goals".equals(stat.getType()))
                .map(stat -> stat.getValue())
                .collect(Collectors.joining(", ")));
        teamStatisticsService.saveTeamStatistics(response);
        em.flush();
        em.clear();

        // when
        final String NEW_XG = "2.5";
        response = updateExpectedGoals(response, NEW_XG);
        log.info("does changed? xg = {}", response.getResponse().get(0).getStatistics().get(0).getStatistics().stream()
                .filter(stat -> "expected_goals".equals(stat.getType()))
                .map(stat -> stat.getValue())
                .collect(Collectors.joining(", ")));
        teamStatisticsService.saveTeamStatistics(response);
        em.flush();
        em.clear();

        // then
        Fixture fixture = getFixture();
        TeamStatistics homeTeamStatistics = getTeamStatistics(fixture, true);
        logXgValues(homeTeamStatistics, "home");
        assertThat(homeTeamStatistics.getExpectedGoalsList()).hasSize(1);
        assertThat(homeTeamStatistics.getExpectedGoalsList().get(0).getXg()).isEqualTo(NEW_XG);
    }

    @DisplayName("xg 값 저장 - elapsed 다르면 새롭게 xg 엔티티 추가 저장")
    @Test
    void accumulateXgValueWhenElapsedDifferent() {
        // given
        final String NEW_XG = "9.9";
        final int PREV_ELAPSED = 10;
        final int NEW_ELAPSED = 11;

        // when
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);
        updateFixtureElapsed(response, PREV_ELAPSED);
        teamStatisticsService.saveTeamStatistics(response);
        em.flush();
        em.clear();

        response = apiCallService.fixtureSingle(FIXTURE_ID);
        updateFixtureElapsed(response, NEW_ELAPSED);
        response = updateExpectedGoals(response, NEW_XG);
        teamStatisticsService.saveTeamStatistics(response);
        em.flush();
        em.clear();

        // then
        Fixture fixture = getFixture();
        TeamStatistics homeTeamStatistics = getTeamStatistics(fixture, true);
        logXgValues(homeTeamStatistics, "home");
        assertThat(homeTeamStatistics.getExpectedGoalsList()).hasSize(2);
        assertThat(homeTeamStatistics.getExpectedGoalsList().get(1).getXg()).isEqualTo(NEW_XG);
    }

    private FixtureSingleResponse updateExpectedGoals(FixtureSingleResponse response, String newXg) {
        response.getResponse().get(0).getStatistics().get(0).getStatistics().stream()
                .filter(stat -> "expected_goals".equals(stat.getType()))
                .forEach(stat -> stat.setValue(newXg));
        return response;
    }

    private void updateFixtureElapsed(FixtureSingleResponse response, int elapsed) {
        response.getResponse().get(0).getFixture().getStatus().setElapsed(elapsed);
    }
}