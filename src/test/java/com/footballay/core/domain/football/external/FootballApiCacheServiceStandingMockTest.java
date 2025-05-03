package com.footballay.core.domain.football.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.footballay.core.domain.football.external.FootballApiCacheService;
import com.footballay.core.domain.football.external.fetch.ApiCallService;
import com.footballay.core.domain.football.external.fetch.response.StandingsResponse;
import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.standings.Standing;
import com.footballay.core.domain.football.repository.LeagueRepository;
import com.footballay.core.domain.football.repository.TeamRepository;
import com.footballay.core.domain.football.repository.standings.StandingsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.footballay.core.domain.football.external.MockStandingsResponseBuilder.buildMockStandingsResponse;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Slf4j
@Transactional
@SpringBootTest
public class FootballApiCacheServiceStandingMockTest {

    @Autowired
    private FootballApiCacheService footballApiCacheService;

    final long LEAGUE_ID = 1L;
    final int LEAGUE_CURRENT_SEASON = 2025;
    final long[] TEAM_IDS = new long[]{11,12,13};

    static private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * jackson 이 기본적으로 Java 8 의 날짜/시간 API 를 지원하지 않기 때문에,
     * OffsetDateTime 을 write 시 에러가 발생합니다.
     * 따라서 JavaTimeModule 을 등록하고 WRITE_DATES_AS_TIMESTAMPS 를 비활성화해서 해결합니다.
     */
    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Autowired
    private StandingsRepository standingsRepository;
    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;

    @MockBean
    private ApiCallService apiCallService;

    @DisplayName("mock 응답을 사용해 Standing Cache 성공")
    @Test
    void successCacheStandingWithMockResponse() throws JsonProcessingException {
        // given
        MockLeagueTeam mockLeagueTeam = createMockLeagueTeam();
        League league = mockLeagueTeam.league;
        long leagueId = league.getLeagueId();
        int season = league.getCurrentSeason();

        StandingsResponse mockResponse = buildMockStandingsResponse(league, mockLeagueTeam.teams);
        when(apiCallService.standings(eq(leagueId),eq(season)))
                .thenReturn(mockResponse);

        // when
        StandingsResponse response = apiCallService.standings(leagueId, season);
        log.info("Mock StandingsResponse: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getStandingData()));
        Standing savedStanding = footballApiCacheService.cacheStandingOfLeague(league.getLeagueId());

        // then
        List<Standing> all = standingsRepository.findAll();
        assertThat(all).hasSize(1);
        Standing foundStanding = all.get(0);
        assertThat(foundStanding.getStandingTeams()).hasSize(3);
        for(var standingTeam : foundStanding.getStandingTeams()) {
            assertThat(standingTeam.getStanding()).isEqualTo(foundStanding);
        }
    }

    protected MockLeagueTeam createMockLeagueTeam() {
        League mockLeague = League.builder()
                .leagueId(LEAGUE_ID)
                .available(true)
                .name("LeagueOne")
                .koreanName("리그원")
                .currentSeason(LEAGUE_CURRENT_SEASON)
                .build();
        mockLeague = leagueRepository.save(mockLeague);

        Team[] teams = new Team[TEAM_IDS.length];
        for(int i = 0 ; i < TEAM_IDS.length ; i++) {
            Team meckTeam = Team.builder()
                    .id(TEAM_IDS[i])
                    .name("team_" + TEAM_IDS[i])
                    .koreanName("팀" + TEAM_IDS[i])
                    .logo("mock://mockdom.com/team/" + TEAM_IDS[i])
                    .build();
            teams[i] = teamRepository.save(meckTeam);
        }

        return new MockLeagueTeam(mockLeague, teams);
    }

    @Getter
    private static class MockLeagueTeam {
        final League league;
        final Team[] teams;

        public MockLeagueTeam(League league, Team... teams) {
            this.league = league;
            this.teams = teams;
        }
    }
}
