package com.gyechunsik.scoreboard.web.football.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.StartLineupTask;
import com.gyechunsik.scoreboard.domain.football.scheduler.live.LiveFixtureProcessor;
import com.gyechunsik.scoreboard.web.football.response.fixture.info.FixtureInfoResponse;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles({"dev","mockapi"})
class FootballStreamDtoMapperTest {

    @Autowired
    private FootballRoot footballRoot;

    @Autowired
    private FootballApiCacheService cacheService;
    @Autowired
    private StartLineupTask startLineupTask;
    @Autowired
    private LiveFixtureProcessor liveFixtureProcessor;

    @Autowired
    private EntityManager em;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        cacheService.cacheLeague(LeagueId.EURO);
        cacheService.cacheTeamsOfLeague(LeagueId.EURO);
        cacheService.cacheTeamSquad(TeamId.SPAIN);
        cacheService.cacheTeamSquad(TeamId.CROATIA);
        cacheService.cacheFixturesOfLeague(LeagueId.EURO);
        startLineupTask.requestAndSaveLineup(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);
        liveFixtureProcessor.requestAndSaveLiveFixtureData(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);

        em.flush();
        em.clear();
    }

    @DisplayName("")
    @Test
    void FixtureInfoResponse() throws JsonProcessingException {
        // given
        final long fixtureId = FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA;
        Fixture fixture = footballRoot.getFixtureWithEager(fixtureId).orElseThrow();

        Integer number = fixture.getLineups().get(0).getStartPlayers().get(0).getPlayer().getNumber();
        log.info("number :: {}", number);

        // when
        FixtureInfoResponse response = FootballStreamDtoMapper.toFixtureInfoResponse(fixture);

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

        log.info("response json :: \n{}", json);

        // then
        assertThat(response.fixtureId()).isEqualTo(fixtureId);
        assertThat(response.league()).isNotNull();
        assertThat(response.home()).isNotNull();
        assertThat(response.away()).isNotNull();
        assertThat(response.date()).isNotNull();
        assertThat(response.liveStatus()).isNotNull();
        assertThat(response.events()).isNotNull();
        assertThat(response.lineup()).isNotNull();
    }
}