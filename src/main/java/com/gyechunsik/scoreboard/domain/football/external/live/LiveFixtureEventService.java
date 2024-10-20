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
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.FixtureEventRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.LiveStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse.*;

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
                .orElseThrow(() -> new IllegalArgumentException("_Fixture 정보가 없습니다. fixtureId=" + fixtureId));
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("_League 정보가 없습니다. leagueId=" + leagueId));
        Team home = teamRepository.findById(homeId)
                .orElseThrow(() -> new IllegalArgumentException("_Home _Team 정보가 없습니다. homeId=" + homeId));
        Team away = teamRepository.findById(awayId)
                .orElseThrow(() -> new IllegalArgumentException("_Away _Team 정보가 없습니다. awayId=" + awayId));

        List<_Events> events = fixtureSingle.getEvents();
        if (events.isEmpty()) {
            log.info("이벤트가 없습니다. fixtureId={}", fixtureId);
            return;
        }

        List<FixtureEvent> fixtureEventList
                = fixtureEventRepository.findByFixtureOrderBySequenceDesc(fixture);
        if (events.size() < fixtureEventList.size()) {
            log.warn("이벤트 수 감소: API 응답 사이즈={}, 저장된 Event 사이즈={}, fixtureId={}",
                    events.size(), fixtureEventList.size(), fixtureId);
            // API 응답에 맞춰서 응답보다 더 많은 이벤트들은 삭제
            for(int i = events.size() ; i < fixtureEventList.size() ; i++) {
                fixtureEventRepository.delete(fixtureEventList.get(i));
            }
            // fixtureEventList 를 size 에 맞게 초과하는 이벤트들은 삭제
            fixtureEventList = fixtureEventList.subList(0, events.size());
        }
        updateExistingEvents(events, fixtureEventList);
        int startSequence = fixtureEventList.size();
        saveEventsFromStartSequence(startSequence, events, fixtureId, fixture);
    }

    private void updateExistingEvents(List<_Events> events, List<FixtureEvent> fixtureEventList) {
        for(int i = 0 ; i < fixtureEventList.size() ; i++) {
            _Events event = events.get(i);
            FixtureEvent fixtureEvent = fixtureEventList.get(i);
            // 이벤트가 다르다면 업데이트
            if(!isSameEvent(event, fixtureEvent)) {
                Team team = teamRepository.findById(event.getTeam().getId())
                        .orElseThrow(() -> new IllegalArgumentException("팀 정보가 없습니다. teamId=" + event.getTeam().getId()));

                // TODO : 감독 경고가 발생할 수 있음. ex. fixtureId=1208095 맨유 텐하으 감독 경고
                Optional<Player> optionalPlayer = playerRepository.findById(event.getPlayer().getId());
                if(optionalPlayer.isEmpty()) {
                    log.warn("선수 정보가 없습니다. playerId={}", event.getPlayer().getId());
                    continue;
                }

                Player player = optionalPlayer.get();
                Long assistId = event.getAssist().getId();
                Player assist = null;
                if(assistId != null && assistId > 0) {
                    Optional<Player> optionalAssist = playerRepository.findById(assistId);
                    if(optionalAssist.isEmpty()) {
                        log.warn("어시스트 선수가 주어졌으나 캐싱되어있지 않습니다. assistId={}", assistId);
                    } else {
                        assist = optionalAssist.get();
                    }
                }
                updateEvent(event, fixtureEvent, team, player, assist);
            }
        }
    }

    private boolean isSameEvent(_Events event, FixtureEvent fixtureEvent) {
        return Objects.equals(event.getTime().getElapsed(), fixtureEvent.getTimeElapsed())
                && Objects.equals(event.getTime().getExtra(), fixtureEvent.getExtraTime())
                && event.getType().equalsIgnoreCase(fixtureEvent.getType().name())
                && event.getDetail().equalsIgnoreCase(fixtureEvent.getDetail())
                && Objects.equals(event.getComments(), fixtureEvent.getComments())
                && event.getTeam().getId().equals(fixtureEvent.getTeam().getId())
                && event.getPlayer().getId().equals(fixtureEvent.getPlayer().getId())
                && (Objects.equals(event.getAssist() == null ? null : event.getAssist().getId(),
                    fixtureEvent.getAssist() == null ? null : fixtureEvent.getAssist().getId()));
    }

    private void updateEvent(_Events event, FixtureEvent fixtureEvent, Team team, Player player, @Nullable Player assist) {
        fixtureEvent.setTeam(team);
        fixtureEvent.setPlayer(player);
        fixtureEvent.setAssist(assist);
        fixtureEvent.setTimeElapsed(event.getTime().getElapsed());
        fixtureEvent.setExtraTime(event.getTime().getExtra() == null ? 0 : event.getTime().getExtra());
        fixtureEvent.setType(EventType.valueOf(event.getType().toUpperCase()));
        fixtureEvent.setDetail(event.getDetail());
        fixtureEvent.setComments(event.getComments());
    }

    private void saveEventsFromStartSequence(int startSequence, List<_Events> events, Long fixtureId, Fixture fixture) {
        for (int sequence = startSequence; sequence < events.size(); sequence++) {
            _Events event = events.get(sequence);
            FixtureEvent fixtureEvent;

            try{
                Team team = teamRepository.findById(event.getTeam().getId())
                        .orElseThrow(() -> new IllegalArgumentException("팀 정보가 없습니다. teamId=" + event.getTeam().getId()));
                if(event.getPlayer().getId() == null) {
                    log.error("Fixture Event 에서 선수 id 가 null 입니다. fixtureId={}, event={}", fixtureId, event);
                    // throw new IllegalArgumentException("Fixture Event 에서 선수 id 가 null 입니다.");
                    continue;
                }

                Optional<Player> optionalPlayer = playerRepository.findById(event.getPlayer().getId());
                if(optionalPlayer.isEmpty()) {
                    log.warn("선수 정보가 없습니다. playerId={}", event.getPlayer().getId());
                    continue;
                }
                Player player = optionalPlayer.get();
                Long assistId = event.getAssist().getId();
                Player assist = null;
                if(assistId != null && assistId > 0) {
                    Optional<Player> optionalAssist = playerRepository.findById(assistId);
                    if(optionalAssist.isEmpty()) {
                        log.warn("어시스트 선수가 주어졌으나 캐싱되어있지 않습니다. assistId={}", assistId);
                    } else {
                        assist = optionalAssist.get();
                    }
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
                log.error("이벤트 변환 실패. fixtureId={}, sequence={}, event={}", fixtureId, sequence, event);
                log.error("event type = {}", event.getType());
                throw e;
            }
        }
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

    /*
    # 동작 구조 / 구현 순서
    ScheduleService(Job등록) -> Job(Task콜) -> Task(실제 로직시작지점) -> Service(엔티티 변환 및 저장 등 로직구현)
    따라서 위 동작 구조 역순으로 구현해나감
    LiveFixtureEventService -> LiveFixtureProcessor -> LiveFixtureTask -> ScheduleService
    각 구현 및 단위테스트 구현하고
    실제로 동작하는지 실제 api 로 테스트 해보자
     */
}
