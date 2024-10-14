package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.MockApiCallServiceImpl;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.live.PlayerStatisticsService;
import com.gyechunsik.scoreboard.domain.football.external.live.TeamStatisticsService;
import com.gyechunsik.scoreboard.domain.football.model.MatchStatistics;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.live.PlayerStatistics;
import com.gyechunsik.scoreboard.domain.football.persistence.live.TeamStatistics;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles({"dev", "mockapi"})
@SpringBootTest
public class FootballRootStatisticsTest {

    @Autowired
    private PlayerStatisticsService playerStatisticsService;

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
    @Autowired
    private PlayerStatisticsRepository playerStatisticsRepository;
    @Autowired
    private FootballRoot footballRoot;

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

        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);
        teamStatisticsService.saveTeamStatistics(response);
        playerStatisticsService.savePlayerStatistics(response);
    }

    private Fixture getFixture() {
        return fixtureRepository.findById(FIXTURE_ID).orElseThrow();
    }

    /**
     * getMatchStatistics의 Eager 로딩 검증 테스트
     * FootballRoot.getMatchStatistics는 메서드 레벨에서 @Transactional을 사용하므로
     * 트랜잭션 외부에서도 반환된 MatchStatistics 내부의 연관 엔티티들이 로딩되어 있어야 합니다.
     * 이 테스트는 이러한 Eager 로딩이 제대로 동작하는지 검증합니다.
     */
    @DisplayName("getMatchStatistics: Eager 로딩 검증")
    @Test
    void getMatchStatisticsWithEagerLoading() {
        // given

        // when
        MatchStatistics matchStatistics = footballRoot.getMatchStatistics(FIXTURE_ID);

        // then
        assertThat(matchStatistics).isNotNull();
        assertThat(matchStatistics.getHome()).isNotNull();
        assertThat(matchStatistics.getAway()).isNotNull();

        TeamStatistics homeStatistics = matchStatistics.getHomeStatistics();
        assertThat(homeStatistics).isNotNull();
        assertThat(homeStatistics.getTeam()).isNotNull();
        assertThat(homeStatistics.getExpectedGoalsList()).isNotEmpty();

        TeamStatistics awayStatistics = matchStatistics.getAwayStatistics();
        assertThat(awayStatistics).isNotNull();
        assertThat(awayStatistics.getTeam()).isNotNull();
        assertThat(awayStatistics.getExpectedGoalsList()).isNotEmpty();

        List<PlayerStatistics> homePlayerStatistics = matchStatistics.getHomePlayerStatistics();
        assertThat(homePlayerStatistics).isNotEmpty();
        homePlayerStatistics.forEach(playerStat -> {
            assertThat(playerStat.getTeam()).isNotNull();
            assertThat(playerStat.getPlayer()).isNotNull();
        });

        List<PlayerStatistics> awayPlayerStatistics = matchStatistics.getAwayPlayerStatistics();
        assertThat(awayPlayerStatistics).isNotEmpty();
        awayPlayerStatistics.forEach(playerStat -> {
            assertThat(playerStat.getTeam()).isNotNull();
            assertThat(playerStat.getPlayer()).isNotNull();
        });
    }
}
