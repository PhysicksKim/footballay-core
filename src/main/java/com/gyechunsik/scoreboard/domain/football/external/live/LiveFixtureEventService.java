package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.EventType;
import com.gyechunsik.scoreboard.domain.football.persistence.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.persistence.live.LiveStatus;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse._FixtureSingle;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.FixtureEventRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.LiveStatusRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse.*;

// TODO : 전부 테스트 작성 재점검 필요
// TODO : Event 저장용 UnregisteredPlayer 인 경우 Lineup 에서 해당 미등록 선수가 있는지 조사하도록 합니다.
//  라인업에서 이름과 번호를 기반으로 매칭되는 선수가 있다면 해당 엔티티를 가져와서 연관관계를 맺도록 해주면 됩니다.
/**
 *
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LiveFixtureEventService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final FixtureRepository fixtureRepository;
    private final LeagueRepository leagueRepository;
    private final FixtureEventRepository fixtureEventRepository;
    private final LiveStatusRepository liveStatusRepository;

    private static final List<String> FINISHED_STATUSES
            = List.of("TBD", "FT", "AET", "PEN", "PST", "CANC", "ABD", "AWD", "WO");
    private final MatchPlayerRepository matchPlayerRepository;

    public void saveLiveEvent(FixtureSingleResponse response) {
        if (response.getResponse().isEmpty()) {
            throw new IllegalArgumentException("API _Response 데이터가 없습니다.");
        }

        _FixtureSingle fixtureSingle = response.getResponse().get(0);

        Long fixtureId = fixtureSingle.getFixture().getId();
        Long leagueId = fixtureSingle.getLeague().getId();
        Long homeId = fixtureSingle.getTeams().getHome().getId();
        Long awayId = fixtureSingle.getTeams().getAway().getId();

        log.info("started to save live event fixtureId={}", fixtureId);

        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Fixture 정보가 없습니다. fixtureId=" + fixtureId));
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League 정보가 없습니다. leagueId=" + leagueId));
        Team home = teamRepository.findById(homeId)
                .orElseThrow(() -> new IllegalArgumentException("Home Team 정보가 없습니다. homeId=" + homeId));
        Team away = teamRepository.findById(awayId)
                .orElseThrow(() -> new IllegalArgumentException("Away Team 정보가 없습니다. awayId=" + awayId));
        log.info("found all fixture league home/away entities of fixtureId={}", fixtureId);

        List<_Events> events = fixtureSingle.getEvents();
        if (events.isEmpty()) {
            log.info("이벤트가 없습니다. fixtureId={}", fixtureId);
            return;
        }

        List<FixtureEvent> fixtureEventList
                = fixtureEventRepository.findByFixtureOrderBySequenceDesc(fixture);
        log.info("found events. fixtureId={}, size={}", fixtureId, fixtureEventList.size());

        // 이벤트가 취소로 인한 이벤트 수 감소 처리 (ex. 카드 취소, 골 취소)
        if (events.size() < fixtureEventList.size()) {
            log.info("이벤트 수 감소 처리. API 응답 size={}, 저장된 Event size={}, fixtureId={}",
                    events.size(), fixtureEventList.size(), fixtureId);

            for (int i = events.size(); i < fixtureEventList.size(); i++) {
                fixtureEventRepository.delete(fixtureEventList.get(i));
            }
            fixtureEventList = fixtureEventList.subList(0, events.size());
        }

        // 기존 이벤트 업데이트
        log.info("try to update existing events. fixtureId={}", fixtureId);
        updateExistingEvents(events, fixtureEventList);

        // 새로운 이벤트 업데이트
        int startSequence = fixtureEventList.size();
        log.info("try to save events from start sequence. fixtureId={}, startSequence={}", fixtureId, startSequence);
        saveEventsFromStartSequence(startSequence, events, fixture);
        log.info("saved live events fixtureId={}", fixtureId);
    }

    /**
     * id : null 인 이벤트를 고려해서 MatchPlayer 에서 Player 연관관계 맺을지 아니면 unregistered player 로 처리할지 분기처리 해야함
     *
     * @param events
     * @param fixtureEventList
     */
    private void updateExistingEvents(List<_Events> events, List<FixtureEvent> fixtureEventList) {
        for (int i = 0; i < fixtureEventList.size(); i++) {
            _Events event = events.get(i);
            FixtureEvent fixtureEvent = fixtureEventList.get(i);
            // 이벤트가 다르다면 업데이트
            if (!isSameEvent(event, fixtureEvent)) {
                Fixture fixture = fixtureEvent.getFixture();
                Team team = teamRepository.findById(event.getTeam().getId())
                        .orElseThrow(() -> new IllegalArgumentException("팀 정보가 없습니다. teamId=" + event.getTeam().getId()));

                // TODO : 감독 경고가 발생할 수 있음. ex. fixtureId=1208095 맨유 텐하으 감독 경고
                Long eventPlayerId = event.getPlayer().getId();
                Long eventAssistId = event.getAssist().getId();
                long fixtureId = fixture.getFixtureId();
                long teamId = team.getId();
                String eventPlayerName = event.getPlayer().getName();
                String eventAssistName = event.getAssist().getName();

                MatchPlayer eventPlayer;
                if(existEventPerson(eventPlayerId, eventPlayerName)) {
                    eventPlayer = getOrCreateMatchPlayerFromEventPlayer(
                            eventPlayerId,
                            eventPlayerName,
                            fixtureId,
                            teamId
                    );
                } else {
                    log.warn("eventPlayer 가 null 입니다. Player 가 Null 인 이벤트에 대한 조사가 필요합니다. fixtureId={}, eventsResponse={}", fixtureId, event);
                    eventPlayer = null;
                }

                MatchPlayer eventAssist = null;
                if(existEventPerson(eventAssistId, eventAssistName)) {
                    eventAssist = getOrCreateMatchPlayerFromEventPlayer(
                            eventAssistId,
                            eventAssistName,
                            fixtureId,
                            teamId
                    );
                }

                logIfEventPersonIsUnexpected(eventPlayer, eventAssist, fixtureId);

                updateEvent(event, fixtureEvent, team, eventPlayer, eventAssist);
            }
        }
    }

    private static boolean existEventPerson(Long eventAssistId, String eventAssistName) {
        return eventAssistId != null || eventAssistName != null;
    }

    private void logIfEventPersonIsUnexpected(@Nullable MatchPlayer eventPlayer, @Nullable MatchPlayer eventAssist, long fixtureId) {
        if (eventPlayer == null) {
            log.warn("eventPlayer 가 null 입니다. fixtureId={}", fixtureId);
        }
        if (eventPlayer != null && eventPlayer.getPlayer() == null) {
            log.warn("eventPlayer 가 unregistered player 입니다. eventPlayer(name={}), fixtureId={}", eventPlayer.getUnregisteredPlayerName(), fixtureId);
        }
        if (eventAssist != null && eventAssist.getPlayer() == null) {
            log.warn("eventAssist 가 unregistered player 입니다. eventAssist(name={}), fixtureId={}", eventAssist.getUnregisteredPlayerName(), fixtureId);
        }
    }

    protected @Nullable MatchPlayer getOrCreateMatchPlayerFromEventPlayer(
            @Nullable Long playerId,
            @Nullable String playerName,
            long fixtureId,
            long teamId
    ) {
        if (playerId == null) {
            if (playerName == null) {
                log.warn("event 에 player id 와 name 이 모두 null 이므로 MatchPlayer 를 생성하지 않습니다. fixtureId={}",
                        fixtureId
                );
                return null;
            }

            MatchPlayer unregisteredPlayer = MatchPlayer.builder()
                    .unregisteredPlayerName(playerName)
                    .build();
            log.info("playerId=null 이므로 event 에서 unregistered MatchPlayer 생성 name={}, fixtureId={}",
                    playerName,
                    fixtureId
            );
            return matchPlayerRepository.save(unregisteredPlayer);
        }

        Optional<Player> findPlayer = playerRepository.findById(playerId);
        if (findPlayer.isEmpty()) {
            log.warn("event 에 등장했고 id 가 주어졌으나 db에 존재하지 않는 선수이므로 unregistered MatchPlayer 로 저장합니다. playerId={}, name={}, fixtureId={}",
                    playerId,
                    playerName,
                    fixtureId
            );

            MatchPlayer unregisteredPlayerButIdExist = MatchPlayer.builder()
                    .unregisteredPlayerName(playerName)
                    .build();
            log.info("id가 존재하는 unregistered player 생성 playerId={}, name={}, fixtureId={}",
                    playerId,
                    playerName,
                    fixtureId
            );
            return matchPlayerRepository.save(unregisteredPlayerButIdExist);
        }

        Player player = findPlayer.get();
        Optional<MatchPlayer> findMatchPlayer
                = matchPlayerRepository
                .findMatchPlayerByFixtureTeamAndPlayer(fixtureId, teamId, playerId);
        if (findMatchPlayer.isPresent()) {
            return findMatchPlayer.get();
        }

        log.warn("""
                        event 로 주어진 player 가 lineup 에서 저장되어있지 않습니다.
                        MatchPlayer 를 생성하지만 Lineup 에 연관관계를 맺지 않고 저장합니다.
                        playerId={}, name={},
                        fixtureId={}, teamId={}, playerId={}
                        """,
                playerId,
                playerName,
                fixtureId,
                teamId,
                playerId
        );
        MatchPlayer matchPlayerNotWithLineup = MatchPlayer.builder()
                .player(player)
                .unregisteredPlayerName(playerName)
                .build();

        log.warn("player 가 lineup 에 없으므로 MatchPlayer 생성 playerId={}, name={}, fixtureId={}",
                playerId,
                playerName,
                fixtureId
        );
        return matchPlayerRepository.save(matchPlayerNotWithLineup);
    }

    // TODO : Test 작성. event 와 fixtureEvent 에서 각각 response 와 dbData 에서 player 와 assist 에 대해 isSame 테스트
    //  isSameEventPlayer, isSameEventAssist 메서드로 분리해서 테스트 작성
    //  둘이 등록 여부 다른 경우 테스트(하나는 unregistered player 일 때, 하나는 registered player 일 때)
    protected boolean isSameEvent(_Events event, FixtureEvent fixtureEvent) {
        boolean isResponsePlayerNull = event.getPlayer() == null || (event.getPlayer().getId() == null && event.getPlayer().getName() == null);
        boolean isDbEntityPlayerNull = fixtureEvent.getPlayer() == null;
        if(isResponsePlayerNull != isDbEntityPlayerNull) {
            return false;
        }
        boolean bothNotNullPlayer = !isResponsePlayerNull;
        if(bothNotNullPlayer) {
            MatchPlayer matchPlayer = fixtureEvent.getPlayer();
            Long responsePlayerId = event.getPlayer().getId();
            String responsePlayerName = event.getPlayer().getName();
            if (isNotSameEventPlayer(responsePlayerId, responsePlayerName, matchPlayer)) {
                return false;
            }
        }

        boolean isResponseAssistNull = event.getAssist() == null || (event.getAssist().getId() == null && event.getAssist().getName() == null);
        boolean isDbEntityAssistNull = fixtureEvent.getAssist() == null;
        if(isResponseAssistNull != isDbEntityAssistNull) {
            return false;
        }
        boolean bothNotNullAssist = !isResponseAssistNull;
        if (bothNotNullAssist) {
            MatchPlayer matchAssist = fixtureEvent.getAssist();
            Long responseAssistId = event.getAssist().getId();
            String responseAssistName = event.getAssist().getName();
            if (isNotSameEventPlayer(responseAssistId, responseAssistName, matchAssist)) {
                return false;
            }
        }

        return isSameEventData(event, fixtureEvent);
    }

    /**
     * event 응답의 person 필드들(player, assist)은 완전히 null 일 수도 있습니다. <br>
     * 따라서 완전히 비어있는 matchPlayer 도 존재할 수 있음을 상정하고 비교해야 합니다.
     *
     * @param responsePlayerId
     * @param responsePlayerName
     * @param matchPlayer
     * @return 다를 경우 true 반환
     */
    private boolean isNotSameEventPlayer(
            @Nullable Long responsePlayerId,
            @Nullable String responsePlayerName,
            @Nullable MatchPlayer matchPlayer
    ) {
        final boolean SAME_PLAYER = false;
        final boolean NOT_SAME_PLAYER = true;

        boolean existResponsePlayer = existEventPerson(responsePlayerId, responsePlayerName);
        boolean existMatchPlayer = matchPlayer != null;

        // 둘 다 존재하지 않는다면
        if (!existResponsePlayer && !existMatchPlayer) {
            return SAME_PLAYER;
        }
        // 서로 존재 여부가 일치하지 않는 경우
        if (existResponsePlayer != existMatchPlayer) {
            return NOT_SAME_PLAYER;
        }

        boolean isResponseNotRegistered = responsePlayerId == null;
        boolean isMatchPlayerNotRegistered = matchPlayer.getPlayer() == null;
        // registeredPlayer 여부가 일치하지 않는 경우
        if (isResponseNotRegistered != isMatchPlayerNotRegistered) {
            return NOT_SAME_PLAYER;
        }

        boolean bothRegistered = !isResponseNotRegistered;
        if (bothRegistered) {
            if (Objects.equals(responsePlayerId, matchPlayer.getPlayer().getId())) {
                return SAME_PLAYER;
            } else {
                return NOT_SAME_PLAYER;
            }
        } else {
            if (Objects.equals(responsePlayerName, matchPlayer.getUnregisteredPlayerName())) {
                return SAME_PLAYER;
            } else {
                return NOT_SAME_PLAYER;
            }
        }
        // Unreachable code
    }

    protected boolean isSameEventData(_Events event, FixtureEvent fixtureEvent) {
        return Objects.equals(event.getTime().getElapsed(), fixtureEvent.getTimeElapsed())
                && Objects.equals(event.getTime().getExtra(), fixtureEvent.getExtraTime())
                && event.getType().equalsIgnoreCase(fixtureEvent.getType().name())
                && event.getDetail().equalsIgnoreCase(fixtureEvent.getDetail())
                && Objects.equals(event.getComments(), fixtureEvent.getComments())
                && event.getTeam().getId().equals(fixtureEvent.getTeam().getId());
    }

    private void updateEvent(_Events event, FixtureEvent fixtureEvent, Team team, @Nullable MatchPlayer player, @Nullable MatchPlayer assist) {
        fixtureEvent.setTeam(team);
        fixtureEvent.setPlayer(player);
        fixtureEvent.setAssist(assist);
        fixtureEvent.setTimeElapsed(event.getTime().getElapsed());
        fixtureEvent.setExtraTime(event.getTime().getExtra() == null ? 0 : event.getTime().getExtra());
        fixtureEvent.setType(EventType.valueOf(event.getType().toUpperCase()));
        fixtureEvent.setDetail(event.getDetail());
        fixtureEvent.setComments(event.getComments());
    }

    /**
     * 새롭게 등장한 이벤트 들을 저장합니다. <br>
     * startSequence 는 response 의 event List 에서 새롭게 저장하기 시작해야 하는 지점의 index 입니다.
     *
     * @param startSequence 새롭게 저장하기 시작해야 하는 index
     * @param events        API 응답의 event List
     * @param fixture       fixture entity
     */
    private void saveEventsFromStartSequence(int startSequence, List<_Events> events, Fixture fixture) {
        Long fixtureId = fixture.getFixtureId();

        if(events.size() <= startSequence) {
            log.info("새로운 이벤트가 없습니다. fixtureId={}", fixtureId);
            return;
        }

        for (int sequence = startSequence; sequence < events.size(); sequence++) {
            _Events event = events.get(sequence);
            FixtureEvent fixtureEvent;

            try {
                Team team = teamRepository.findById(event.getTeam().getId())
                        .orElseThrow(() -> new IllegalArgumentException("팀 정보가 없습니다. teamId=" + event.getTeam().getId()));
                Long playerId = event.getPlayer().getId();
                String playerName = event.getPlayer().getName();
                MatchPlayer player = createEventPlayer(playerId, playerName, fixtureId, team.getId());

                Long assistId = event.getAssist().getId();
                String assistName = event.getAssist().getName();
                MatchPlayer assist = createEventPlayer(assistId, assistName, fixtureId, team.getId());

                if(player != null) {
                    player = matchPlayerRepository.save(player);
                }
                if(assist != null) {
                    assist = matchPlayerRepository.save(assist);
                }

                fixtureEvent = FixtureEvent.builder()
                        .fixture(fixture)
                        .team(team)
                        .player(player)
                        .assist(assist)
                        .sequence(sequence)
                        .timeElapsed(event.getTime().getElapsed())
                        .extraTime(event.getTime().getExtra() == null ? 0 : event.getTime().getExtra())
                        .type(EventType.valueOf(event.getType().toUpperCase()))
                        .detail(event.getDetail())
                        .comments(event.getComments())
                        .build();
                fixtureEventRepository.save(fixtureEvent);
            } catch (Exception e) {
                log.error("이벤트 변환 실패. 빈 FixtureEvent 로 대체합니다. fixtureId={}, sequence={}, event={}", fixtureId, sequence, event, e);
                try {
                    fixtureEvent = FixtureEvent.builder()
                            .fixture(fixture)
                            .team(null)
                            .player(null)
                            .assist(null)
                            .sequence(sequence)
                            .timeElapsed(0)
                            .extraTime(0)
                            .type(EventType.UNKNOWN)
                            .detail("알 수 없는 이벤트")
                            .comments("알 수 없는 이벤트")
                            .build();
                    fixtureEventRepository.save(fixtureEvent);
                } catch (Exception exception) {
                    log.error("빈 이벤트로 저장 실패. fixtureId={}, sequence={}, event={}", fixtureId, sequence, event);
                }
            }
        }
    }

    private @Nullable MatchPlayer createEventPlayer(@Nullable Long id, @Nullable String name, Long fixtureId, long teamId) {
        boolean isEmptyPlayer = id == null && name == null;
        if (isEmptyPlayer) {
            return null;
        }

        boolean isUnregisteredPlayer = id == null;
        if (isUnregisteredPlayer) {
            log.info("event player 가 unregistered player 입니다. fixtureId={}", fixtureId);
            return MatchPlayer.builder()
                    .unregisteredPlayerName(name)
                    .build();
        }

        Optional<MatchPlayer> findMatchPlayer = matchPlayerRepository.findMatchPlayerByFixtureTeamAndPlayer(fixtureId, teamId, id);
        if (findMatchPlayer.isEmpty()) {
            Optional<Player> findPlayer = playerRepository.findById(id);
            if (findPlayer.isEmpty()) {
                log.warn("event player id 가 존재하지만 일치하는 player 가 db 에 존재하지 않습니다. unregistered player 로 MatchPlayer 를 생성합니다. fixtureId={}, playerId={}, name={}", fixtureId, id, name);
                return MatchPlayer.builder()
                        .unregisteredPlayerName(name)
                        .build();
            }

            log.warn("event player id 가 존재하지만 MatchPlayer 가 존재하지 않습니다. Lineup 연관관계를 맺지 않은 registered MatchPlayer 를 생성합니다. fixtureId={}, playerId={}, name={}", fixtureId, id, name);
            return MatchPlayer.builder()
                    .player(findPlayer.get())
                    .substitute(false)
                    .build();
        }

        return findMatchPlayer.get();
    }

    /**
     * 기존에 저장된 fixture 의 live event 에 문제가 감지된 경우 기존 데이터를 삭제하고 다시 저장합니다.
     * @param response API 응답
     */
    public void resolveFixtureEventIntegrityError(FixtureSingleResponse response) {
        log.info("try to resolve fixture event integrity error");
        this.deleteExisingFixtureEventsAndReSaveAllEvents(response);
    }

    private void deleteExisingFixtureEventsAndReSaveAllEvents(FixtureSingleResponse response) {
        Fixture fixture = fixtureRepository.findById(response.getResponse().get(0).getFixture().getId())
                .orElseThrow(() -> new IllegalArgumentException("기존에 캐싱된 fixture 정보가 없습니다."));

        // 기존에 저장된 fixture 의 live event 를 모두 삭제
        List<FixtureEvent> fixtureEventList = fixtureEventRepository.findByFixtureOrderBySequenceDesc(fixture);
        fixtureEventRepository.deleteAll(fixtureEventList);

        // 새로운 fixture 정보를 기반으로 live event 를 다시 저장
        saveLiveEvent(response);
    }

    /**
     * LiveStatus 엔티티를 업데이트합니다
     * @return 경기가 끝났는지 여부
     */
    public boolean updateLiveStatus(FixtureSingleResponse response) {
        _FixtureSingle fixtureSingle = response.getResponse().get(0);
        Long fixtureId = fixtureSingle.getFixture().getId();
        _Status status = fixtureSingle.getFixture().getStatus();
        _Goals goals = fixtureSingle.getGoals();
        log.info("started to update live status. fixtureId={}, status={}", fixtureId, status.getShortStatus());

        Fixture fixture = fixtureRepository.findById(fixtureId).orElseThrow();
        LiveStatus liveStatus = liveStatusRepository.findLiveStatusByFixture(fixture).orElseThrow();
        updateLiveStatusEntity(liveStatus, status, goals);
        status.getElapsed();
        log.info("updated live status. fixtureId={}, status={}, timeElapsed={}",
                fixtureId, status.getShortStatus(), status.getElapsed());
        return isFixtureFinished(status.getShortStatus());
    }

    private void updateLiveStatusEntity(LiveStatus liveStatus, _Status status, _Goals goals) {
        liveStatus.setElapsed(status.getElapsed());
        liveStatus.setLongStatus(status.getLongStatus());
        liveStatus.setShortStatus(status.getShortStatus());
        liveStatus.setHomeScore(goals.getHome());
        liveStatus.setAwayScore(goals.getAway());
    }

    private boolean isFixtureFinished(String shortStatus) {
        return FINISHED_STATUSES.contains(shortStatus);
    }
}
