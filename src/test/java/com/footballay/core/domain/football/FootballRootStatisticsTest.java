package com.footballay.core.domain.football;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballay.core.domain.football.constant.FixtureId;
import com.footballay.core.domain.football.external.FootballApiCacheService;
import com.footballay.core.domain.football.external.fetch.ApiCallService;
import com.footballay.core.domain.football.external.fetch.MockApiCallServiceImpl;
import com.footballay.core.domain.football.external.fetch.response.FixtureSingleResponse;
import com.footballay.core.domain.football.external.lineup.LineupService;
import com.footballay.core.domain.football.external.live.PlayerStatisticsService;
import com.footballay.core.domain.football.external.live.TeamStatisticsService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiCallService = new MockApiCallServiceImpl(objectMapper);
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

    // TODO : Remove after FullMatchStatistics refactoring
    // /**
    //  * getMatchStatistics 의 Eager 로딩 검증 테스트
    //  * FootballRoot.getMatchStatistics 는 메서드 레벨에서 @Transactional 을 사용하므로
    //  * 트랜잭션 외부에서도 반환된 MatchStatisticsDto 내부의 연관 엔티티들이 로딩되어 있어야 합니다.
    //  * 이 테스트는 이러한 Eager 로딩이 제대로 동작하는지 검증합니다.
    //  */
    // @DisplayName("getMatchStatistics: Eager 로딩 검증")
    // @Test
    // void getMatchStatisticsWithEagerLoading() {
    //     // given
    //
    //     // when
    //     MatchStatisticsDto matchStatisticsDTO = footballRoot.getMatchStatistics(FIXTURE_ID);
    //
    //     // then
    //     assertThat(matchStatisticsDTO).isNotNull();
    //     assertThat(matchStatisticsDTO.getHome()).isNotNull();
    //     assertThat(matchStatisticsDTO.getAway()).isNotNull();
    //
    //     MatchStatisticsDto.MatchStatsTeamStatistics homeStatistics = matchStatisticsDTO.getHomeStatistics();
    //     assertThat(homeStatistics).isNotNull();
    //     assertThat(homeStatistics.getBallPossession()).isNotNull();
    //     assertThat(homeStatistics.getExpectedGoalsList()).isNotEmpty();
    //
    //     MatchStatisticsDto.MatchStatsTeamStatistics awayStatistics = matchStatisticsDTO.getAwayStatistics();
    //     assertThat(awayStatistics).isNotNull();
    //     assertThat(awayStatistics.getBallPossession()).isNotNull();
    //     assertThat(awayStatistics.getExpectedGoalsList()).isNotEmpty();
    //
    //     List<MatchStatisticsDto.MatchStatsPlayers> homePlayerStatistics = matchStatisticsDTO.getHomePlayerStatistics();
    //     assertThat(homePlayerStatistics).isNotEmpty();
    //     homePlayerStatistics.forEach(playerStat -> {
    //         assertThat(playerStat).isNotNull();
    //     });
    //
    //     List<MatchStatisticsDto.MatchStatsPlayers> awayPlayerStatistics = matchStatisticsDTO.getAwayPlayerStatistics();
    //     assertThat(awayPlayerStatistics).isNotEmpty();
    //     awayPlayerStatistics.forEach(playerStat -> {
    //         assertThat(playerStat).isNotNull();
    //     });
    // }
}
