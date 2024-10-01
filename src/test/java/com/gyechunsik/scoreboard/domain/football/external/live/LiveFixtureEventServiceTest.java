package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.MockApiCallServiceImpl;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.FixtureEventRepository;
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

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse.*;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@Transactional
@ActiveProfiles({"dev","mockapi"})
@SpringBootTest
class LiveFixtureEventServiceTest {

    @Autowired
    private LiveFixtureEventService liveFixtureEventService;
    @Autowired
    private FootballApiCacheService footballApiCacheService;

    @Autowired
    private FixtureEventRepository fixtureEventRepository;
    @Autowired
    private FixtureRepository fixtureRepository;

    @Autowired
    private EntityManager em;

    private ApiCallService apiCallService;

    private static final long FIXTURE_ID = FixtureId.FIXTURE_SINGLE_1145526;

    @BeforeEach
    public void setup() {
        apiCallService = new MockApiCallServiceImpl();

        footballApiCacheService.cacheLeague(4L);
        footballApiCacheService.cacheTeamsOfLeague(4L);
        footballApiCacheService.cacheTeamSquad(777);
        footballApiCacheService.cacheTeamSquad(27);
        footballApiCacheService.cacheFixturesOfLeague(4L);

        em.clear();
        em.flush();
    }

    @DisplayName("기본적으로 이벤트 저장이 이뤄지는지 확인합니다")
    @Test
    void save() {
        // given
        // Already set by @BeforeEach

        // when
        FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);
        liveFixtureEventService.saveLiveEvent(response);

        // then
        Fixture fixture = fixtureRepository.findById(FIXTURE_ID).orElseThrow();
        fixtureEventRepository.findByFixtureOrderBySequenceDesc(fixture)
                .forEach(fixtureEvent -> log.info("fixtureEvent={}", fixtureEvent));
    }

    @DisplayName("동일한 수의 이벤트 요청이 있을 때 중복 저장이 되지 않음을 확인")
    @Test
    void saveLiveEventAgain() {
        // given
        FixtureSingleResponse initialResponse = apiCallService.fixtureSingle(FIXTURE_ID);
        liveFixtureEventService.saveLiveEvent(initialResponse);
        em.flush();
        em.clear();

        // when
        FixtureSingleResponse subsequentResponse = apiCallService.fixtureSingle(FIXTURE_ID);
        liveFixtureEventService.saveLiveEvent(subsequentResponse);

        // then
        List<_Events> events = subsequentResponse.getResponse().get(0).getEvents();
        int eventCount = events.size();

        Fixture fixture = fixtureRepository.findById(FIXTURE_ID).orElseThrow();
        List<FixtureEvent> savedEvents = fixtureEventRepository.findByFixtureOrderBySequenceDesc(fixture);
        int savedEventCount = savedEvents.size();

        assertThat(savedEventCount).isEqualTo(eventCount);
    }

    @DisplayName("저장된 이벤트 수 보다 적은 수의 요청이 들어온다면 오버된 이벤트는 삭제")
    @Test
    void 응답의이벤트가저장된수보다적을때() {
        // given
        FixtureSingleResponse prevResponse = apiCallService.fixtureSingle(FIXTURE_ID);
        List<_Events> eventsOfPrevResponse = prevResponse.getResponse().get(0).getEvents();
        liveFixtureEventService.saveLiveEvent(prevResponse);
        em.flush();
        em.clear();

        // when
        FixtureSingleResponse subsequentResponse = apiCallService.fixtureSingle(FIXTURE_ID);
        List<_Events> events = subsequentResponse.getResponse().get(0).getEvents();
        events.remove(events.size()-1);
        subsequentResponse.getResponse().get(0).setEvents(events);

        liveFixtureEventService.saveLiveEvent(subsequentResponse);

        // then
        Fixture fixture = fixtureRepository.findById(FIXTURE_ID).orElseThrow();
        List<FixtureEvent> savedEvents = fixtureEventRepository.findByFixtureOrderBySequenceDesc(fixture);

        int prevEventsCount = eventsOfPrevResponse.size();
        int savedEventCount = savedEvents.size();
        int eventCount = events.size();

        assertThat(prevEventsCount).isNotEqualTo(eventCount);
        assertThat(savedEventCount).isEqualTo(eventCount);
        assertThat(savedEventCount).isLessThan(prevEventsCount);
    }
}