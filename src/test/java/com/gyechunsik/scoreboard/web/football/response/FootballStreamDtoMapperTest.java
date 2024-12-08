package com.gyechunsik.scoreboard.web.football.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.dto.FixtureEventWithPlayerDto;
import com.gyechunsik.scoreboard.domain.football.dto.FixtureInfoDto;
import com.gyechunsik.scoreboard.domain.football.dto.FixtureWithLineupDto;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.PreviousMatchTask;
import com.gyechunsik.scoreboard.domain.football.scheduler.live.LiveMatchProcessor;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureEventsResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureInfoResponse;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private PreviousMatchTask previousMatchTask;
    @Autowired
    private LiveMatchProcessor liveMatchProcessor;

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
        previousMatchTask.requestAndSaveLineup(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);
        liveMatchProcessor.requestAndSaveLiveMatchData(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);

        em.flush();
        em.clear();
    }

    @DisplayName("Fixture 정보를 제공하는 응답을 생성합니다")
    @Test
    void FixtureInfoResponse() throws JsonProcessingException {
        // given
        final long fixtureId = FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA;
        FixtureInfoDto fixture = footballRoot.getFixtureInfo(fixtureId).orElseThrow();

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
    }


    @DisplayName("Fixture 이벤트 정보를 제공하는 응답을 생성합니다")
    @Test
    void FixtureEventsResponse() throws JsonProcessingException {
        // given
        final long fixtureId = FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA;
        List<FixtureEventWithPlayerDto> events = footballRoot.getFixtureEvents(fixtureId);

        // when
        FixtureEventsResponse response = FootballStreamDtoMapper.toFixtureEventsResponse(fixtureId, events);

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

        log.info("response json :: \n{}", json);

        // then
        assertThat(response.fixtureId()).isEqualTo(fixtureId);
        assertThat(response.events()).isNotNull();
        assertThat(response.events()).isNotEmpty();

        // 추가적인 이벤트 상세 검증
        FixtureEventsResponse._Events firstEvent = response.events().get(0);
        assertThat(firstEvent.team()).isNotNull();
        assertThat(firstEvent.player()).isNotNull();
        assertThat(firstEvent.type()).isNotNull();
    }
}