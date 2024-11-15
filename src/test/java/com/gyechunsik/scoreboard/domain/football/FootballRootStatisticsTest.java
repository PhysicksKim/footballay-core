package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.MockApiCallServiceImpl;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.lineup.LineupService;
import com.gyechunsik.scoreboard.domain.football.external.live.PlayerStatisticsService;
import com.gyechunsik.scoreboard.domain.football.external.live.TeamStatisticsService;
import com.gyechunsik.scoreboard.domain.football.dto.MatchStatisticsDTO;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.live.PlayerStatistics;
import com.gyechunsik.scoreboard.domain.football.persistence.live.TeamStatistics;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.PlayerStatisticsRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.TeamStatisticsRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
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
    private EntityManager em;

    private ApiCallService apiCallService;

    private static final long FIXTURE_ID = FixtureId.FIXTURE_SINGLE_1145526;

    @Autowired
    private FootballRoot footballRoot;
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

        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);
        lineupService.saveLineup(response);
        teamStatisticsService.saveTeamStatistics(response);
        playerStatisticsService.savePlayerStatistics(response);
    }

    /**
     * getMatchStatistics 의 Eager 로딩 검증 테스트
     * FootballRoot.getMatchStatistics 는 메서드 레벨에서 @Transactional 을 사용하므로
     * 트랜잭션 외부에서도 반환된 MatchStatisticsDTO 내부의 연관 엔티티들이 로딩되어 있어야 합니다.
     * 이 테스트는 이러한 Eager 로딩이 제대로 동작하는지 검증합니다.
     */
    @DisplayName("getMatchStatistics: Eager 로딩 검증")
    @Test
    void getMatchStatisticsWithEagerLoading() {
        // given

        // when
        MatchStatisticsDTO matchStatisticsDTO = footballRoot.getMatchStatistics(FIXTURE_ID);

        // then
        assertThat(matchStatisticsDTO).isNotNull();
        assertThat(matchStatisticsDTO.getHome()).isNotNull();
        assertThat(matchStatisticsDTO.getAway()).isNotNull();

        MatchStatisticsDTO.TeamStatisticsDTO homeStatistics = matchStatisticsDTO.getHomeStatistics();
        assertThat(homeStatistics).isNotNull();
        assertThat(homeStatistics.getBallPossession()).isNotNull();
        assertThat(homeStatistics.getExpectedGoalsList()).isNotEmpty();

        MatchStatisticsDTO.TeamStatisticsDTO awayStatistics = matchStatisticsDTO.getAwayStatistics();
        assertThat(awayStatistics).isNotNull();
        assertThat(awayStatistics.getBallPossession()).isNotNull();
        assertThat(awayStatistics.getExpectedGoalsList()).isNotEmpty();

        List<MatchStatisticsDTO.MatchPlayerStatisticsDTO> homePlayerStatistics = matchStatisticsDTO.getHomePlayerStatistics();
        assertThat(homePlayerStatistics).isNotEmpty();
        homePlayerStatistics.forEach(playerStat -> {
            assertThat(playerStat).isNotNull();
        });

        List<MatchStatisticsDTO.MatchPlayerStatisticsDTO> awayPlayerStatistics = matchStatisticsDTO.getAwayPlayerStatistics();
        assertThat(awayPlayerStatistics).isNotEmpty();
        awayPlayerStatistics.forEach(playerStat -> {
            assertThat(playerStat).isNotNull();
        });
    }
}
