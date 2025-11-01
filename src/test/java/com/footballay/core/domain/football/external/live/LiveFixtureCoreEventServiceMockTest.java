package com.footballay.core.domain.football.external.live;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballay.core.domain.football.constant.FixtureId;
import com.footballay.core.domain.football.external.fetch.ApiCallService;
import com.footballay.core.domain.football.external.fetch.MockApiCallServiceImpl;
import com.footballay.core.domain.football.external.fetch.response.FixtureSingleResponse;
import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.live.FixtureEvent;
import com.footballay.core.domain.football.repository.FixtureRepository;
import com.footballay.core.domain.football.repository.LeagueRepository;
import com.footballay.core.domain.football.repository.PlayerRepository;
import com.footballay.core.domain.football.repository.TeamRepository;
import com.footballay.core.domain.football.repository.live.FixtureEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static com.footballay.core.domain.football.external.fetch.response.FixtureSingleResponse._Events;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LiveFixtureEventServiceMockTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LiveFixtureEventServiceMockTest.class);
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private FixtureRepository fixtureRepository;
    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private FixtureEventRepository fixtureEventRepository;
    @Mock
    private PlayerRepository playerRepository;
    @InjectMocks
    private LiveFixtureEventService liveFixtureEventService;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ApiCallService mockApiCallService = new MockApiCallServiceImpl(objectMapper);

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSaveLiveEvent() {
        // Prepare test data
        Fixture fixture = new Fixture();
        League league = new League();
        Team home = new Team();
        Team away = new Team();
        List<FixtureEvent> fixtureEventList = new ArrayList<>();
        Player player = new Player();
        FixtureSingleResponse response = mockApiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);
        // Mock the repositories
        when(fixtureRepository.findById(any(Long.class))).thenReturn(Optional.of(fixture));
        when(leagueRepository.findById(any(Long.class))).thenReturn(Optional.of(league));
        when(teamRepository.findById(any(Long.class))).thenReturn(Optional.of(home), Optional.of(away));
        when(fixtureEventRepository.findByFixtureOrderBySequenceDesc(any(Fixture.class))).thenReturn(fixtureEventList);
        when(playerRepository.findById(any(Long.class))).thenReturn(Optional.of(player));
        // Call the method under test
        liveFixtureEventService.saveLiveEvent(response);
        // Verify the interactions with the repositories
        List<_Events> events = response.getResponse().get(0).getEvents();
        verify(fixtureRepository, times(1)).findById(any(Long.class));
        verify(leagueRepository, times(1)).findById(any(Long.class));
        verify(fixtureEventRepository, times(1)).findByFixtureOrderBySequenceDesc(any(Fixture.class));
    }
}
