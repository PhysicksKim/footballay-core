    package com.gyechunsik.scoreboard.domain.football.external.live;

    import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
    import com.gyechunsik.scoreboard.domain.football.external.lineup.LineupService;
    import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
    import com.gyechunsik.scoreboard.domain.football.persistence.Player;
    import com.gyechunsik.scoreboard.domain.football.persistence.Team;
    import com.gyechunsik.scoreboard.domain.football.persistence.live.EventType;
    import com.gyechunsik.scoreboard.domain.football.persistence.live.FixtureEvent;
    import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
    import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
    import com.gyechunsik.scoreboard.domain.football.external.fetch.MockApiCallServiceImpl;
    import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
    import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
    import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
    import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
    import com.gyechunsik.scoreboard.domain.football.repository.live.FixtureEventRepository;
    import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
    import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureInfoResponse;
    import jakarta.persistence.EntityManager;
    import lombok.extern.slf4j.Slf4j;

    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Disabled;
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
        @Autowired
        private LineupService lineupService;
        @Autowired
        private PlayerRepository playerRepository;
        @Autowired
        private MatchPlayerRepository matchPlayerRepository;

        @BeforeEach
        public void setup() {
            apiCallService = new MockApiCallServiceImpl();

            footballApiCacheService.cacheLeague(4L);
            footballApiCacheService.cacheTeamsOfLeague(4L);
            footballApiCacheService.cacheTeamSquad(777);
            footballApiCacheService.cacheTeamSquad(27);
            footballApiCacheService.cacheFixturesOfLeague(4L);
        }

        @DisplayName("기본적으로 이벤트 저장이 이뤄지는지 확인합니다")
        @Test
        void save() {
            // given
            FixtureSingleResponse response = apiCallService.fixtureSingle(FIXTURE_ID);
            lineupService.saveLineup(response);

            em.flush();
            em.clear();

            // when
            liveFixtureEventService.saveLiveEvent(response);

            em.flush();
            em.clear();

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
            lineupService.saveLineup(initialResponse);
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
            lineupService.saveLineup(prevResponse);
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

        // --------------

        @DisplayName("isSameEvent: 이벤트와 fixtureEvent에 동일한 등록된 선수가 있을 때, true를 반환해야 한다.")
        @Test
        void isSameEvent_SameRegisteredPlayer_ShouldReturnTrue() {
            // given
            // 등록된 Player 생성 및 저장
            Long playerId = 100L;
            String playerName = "Player 1";
            Player player = new Player();
            player.setId(playerId);
            player.setName(playerName);
            playerRepository.save(player);

            // MatchPlayer 생성 및 저장
            MatchPlayer matchPlayer = new MatchPlayer();
            matchPlayer.setPlayer(player);
            matchPlayerRepository.save(matchPlayer);

            // 이벤트 생성 (등록된 선수 포함)
            _Events._Player eventPlayer = new _Events._Player();
            eventPlayer.setId(playerId);
            eventPlayer.setName(playerName);
            _Events event = createEvent(eventPlayer, null, "Goal", "Normal Goal", 10, 0, "Team A", 1L);

            // FixtureEvent 생성 (등록된 MatchPlayer 포함)
            FixtureEvent fixtureEvent = createFixtureEvent(matchPlayer, null, EventType.GOAL, "Normal Goal", 10, 0, "Team A", 1L);

            // when
            boolean result = liveFixtureEventService.isSameEvent(event, fixtureEvent);

            // then
            assertThat(result).isTrue();
        }

        @DisplayName("isSameEvent: 이벤트와 fixtureEvent에 서로 다른 등록된 선수가 있을 때, false를 반환해야 한다.")
        @Test
        void isSameEvent_DifferentRegisteredPlayers_ShouldReturnFalse() {
            // given
            // 등록된 Player 생성 및 저장
            Long playerId1 = 100L;
            String playerName1 = "Player 1";
            Player player1 = new Player();
            player1.setId(playerId1);
            player1.setName(playerName1);
            playerRepository.save(player1);

            Long playerId2 = 101L;
            String playerName2 = "Player 2";
            Player player2 = new Player();
            player2.setId(playerId2);
            player2.setName(playerName2);
            playerRepository.save(player2);

            // MatchPlayer 생성 및 저장 (fixtureEvent에 사용)
            MatchPlayer matchPlayer = new MatchPlayer();
            matchPlayer.setPlayer(player2); // 다른 선수
            matchPlayerRepository.save(matchPlayer);

            // 이벤트 생성 (player1 사용)
            _Events._Player eventPlayer = new _Events._Player();
            eventPlayer.setId(playerId1);
            eventPlayer.setName(playerName1);
            _Events event = createEvent(eventPlayer, null, "Goal", "Normal Goal", 10, 0, "Team A", 1L);

            // FixtureEvent 생성 (matchPlayer 사용, player2)
            FixtureEvent fixtureEvent = createFixtureEvent(matchPlayer, null, EventType.GOAL, "Normal Goal", 10, 0, "Team A", 1L);

            // when
            boolean result = liveFixtureEventService.isSameEvent(event, fixtureEvent);

            // then
            assertThat(result).isFalse();
        }

        @DisplayName("isSameEvent: 이벤트와 fixtureEvent에 동일한 미등록 선수가 있을 때, true를 반환해야 한다.")
        @Test
        void isSameEvent_SameUnregisteredPlayer_ShouldReturnTrue() {
            // given
            // MatchPlayer 생성 (미등록 선수)
            String unregisteredPlayerName = "Unknown Player";
            MatchPlayer matchPlayer = new MatchPlayer();
            matchPlayer.setUnregisteredPlayerName(unregisteredPlayerName);
            matchPlayerRepository.save(matchPlayer);

            // 이벤트 생성 (미등록 선수)
            _Events._Player eventPlayer = new _Events._Player();
            eventPlayer.setId(null);
            eventPlayer.setName(unregisteredPlayerName);
            _Events event = createEvent(eventPlayer, null, "Goal", "Normal Goal", 10, 0, "Team A", 1L);

            // FixtureEvent 생성 (미등록 MatchPlayer 포함)
            FixtureEvent fixtureEvent = createFixtureEvent(matchPlayer, null, EventType.GOAL, "Normal Goal", 10, 0, "Team A", 1L);

            // when
            boolean result = liveFixtureEventService.isSameEvent(event, fixtureEvent);

            // then
            assertThat(result).isTrue();
        }

        @DisplayName("isSameEvent: 이벤트에 등록된 선수가 있고 fixtureEvent에 미등록 선수가 있을 때, false를 반환해야 한다.")
        @Test
        void isSameEvent_RegisteredEventPlayer_UnregisteredFixtureEventPlayer_ShouldReturnFalse() {
            // given
            // 등록된 Player 생성 및 저장
            Long playerId = 100L;
            String playerName = "Player 1";
            Player player = new Player();
            player.setId(playerId);
            player.setName(playerName);
            playerRepository.save(player);

            // 이벤트 생성 (등록된 선수)
            _Events._Player eventPlayer = new _Events._Player();
            eventPlayer.setId(playerId);
            eventPlayer.setName(playerName);
            _Events event = createEvent(eventPlayer, null, "Goal", "Normal Goal", 10, 0, "Team A", 1L);

            // FixtureEvent 생성 (미등록 MatchPlayer)
            String unregisteredPlayerName = "Unknown Player";
            MatchPlayer matchPlayer = new MatchPlayer();
            matchPlayer.setUnregisteredPlayerName(unregisteredPlayerName);
            matchPlayerRepository.save(matchPlayer);

            FixtureEvent fixtureEvent = createFixtureEvent(matchPlayer, null, EventType.GOAL, "Normal Goal", 10, 0, "Team A", 1L);

            // when
            boolean result = liveFixtureEventService.isSameEvent(event, fixtureEvent);

            // then
            assertThat(result).isFalse();
        }

        @DisplayName("isSameEvent: 이벤트에 미등록 선수가 있고 fixtureEvent에 등록된 선수가 있을 때, false를 반환해야 한다.")
        @Test
        void isSameEvent_UnregisteredEventPlayer_RegisteredFixtureEventPlayer_ShouldReturnFalse() {
            // given
            // 등록된 Player 생성 및 저장
            Long playerId = 100L;
            String playerName = "Player 1";
            Player player = new Player();
            player.setId(playerId);
            player.setName(playerName);
            playerRepository.save(player);

            // MatchPlayer 생성 및 저장 (등록된 선수)
            MatchPlayer matchPlayer = new MatchPlayer();
            matchPlayer.setPlayer(player);
            matchPlayerRepository.save(matchPlayer);

            // 이벤트 생성 (미등록 선수)
            String unregisteredPlayerName = "Unknown Player";
            _Events._Player eventPlayer = new _Events._Player();
            eventPlayer.setId(null);
            eventPlayer.setName(unregisteredPlayerName);
            _Events event = createEvent(eventPlayer, null, "Goal", "Normal Goal", 10, 0, "Team A", 1L);

            // FixtureEvent 생성 (등록된 MatchPlayer)
            FixtureEvent fixtureEvent = createFixtureEvent(matchPlayer, null, EventType.GOAL, "Normal Goal", 10, 0, "Team A", 1L);

            // when
            boolean result = liveFixtureEventService.isSameEvent(event, fixtureEvent);

            // then
            assertThat(result).isFalse();
        }

        // Helper methods to create event and fixtureEvent
        private _Events createEvent(_Events._Player player, _Events._Assist assist, String type, String detail, int elapsed, Integer extra, String teamName, Long teamId) {
            _Events event = new _Events();
            event.setPlayer(player);
            event.setAssist(assist);
            event.setType(type);
            event.setDetail(detail);
            _Events._Time time = new _Events._Time();
            time.setElapsed(elapsed);
            time.setExtra(extra);
            event.setTime(time);
            FixtureSingleResponse._Events._Team team = new FixtureSingleResponse._Events._Team();
            team.setId(teamId);
            team.setName(teamName);
            team.setLogo("logoUrl");
            event.setTeam(team);
            return event;
        }

        private FixtureEvent createFixtureEvent(MatchPlayer player, MatchPlayer assist, EventType type, String detail, int elapsed, int extraTime, String teamName, Long teamId) {
            FixtureEvent fixtureEvent = new FixtureEvent();
            fixtureEvent.setPlayer(player);
            fixtureEvent.setAssist(assist);
            fixtureEvent.setType(type);
            fixtureEvent.setDetail(detail);
            fixtureEvent.setTimeElapsed(elapsed);
            fixtureEvent.setExtraTime(extraTime);
            Team team = new Team();
            team.setId(teamId);
            team.setName(teamName);
            fixtureEvent.setTeam(team);
            return fixtureEvent;
        }
    }