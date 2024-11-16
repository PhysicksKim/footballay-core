package com.gyechunsik.scoreboard.web.football.response;

import com.gyechunsik.scoreboard.domain.football.comparator.StartLineupComparator;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.*;
import com.gyechunsik.scoreboard.utils.TimeConverter;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureEventsResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureInfoResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLineupResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLineupResponse._Lineup;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLiveStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.gyechunsik.scoreboard.web.football.response.fixture.FixtureInfoResponse.*;

@Slf4j
public class FootballStreamDtoMapper {

    public static LeagueResponse toLeagueResponse(League league) {
        return new LeagueResponse(
                league.getLeagueId(),
                league.getName(),
                league.getKoreanName(),
                league.getLogo(),
                league.getCurrentSeason()
        );
    }

    public static FixtureOfLeagueResponse toFixtureOfLeagueResponse(Fixture fixture) {
        if (fixture.getHomeTeam() == null || fixture.getAwayTeam() == null) {
            throw new IllegalArgumentException("홈팀 또는 어웨이팀 정보가 존재하지 않습니다. homeTeamIsNull:" + (fixture.getHomeTeam() == null) + ", awayTeamIsNull:" + (fixture.getAwayTeam() == null));
        }
        if (fixture.getLiveStatus() == null) {
            throw new IllegalArgumentException("라이브 상태 정보가 존재하지 않습니다.");
        }

        LiveStatus liveStatus = fixture.getLiveStatus();
        FixtureOfLeagueResponse._Match match = new FixtureOfLeagueResponse._Match(
                fixture.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                fixture.getRound()
        );
        FixtureOfLeagueResponse._Team home = new FixtureOfLeagueResponse._Team(
                fixture.getHomeTeam().getName(),
                fixture.getHomeTeam().getLogo(),
                fixture.getHomeTeam().getKoreanName()
        );
        FixtureOfLeagueResponse._Team away = new FixtureOfLeagueResponse._Team(
                fixture.getAwayTeam().getName(),
                fixture.getAwayTeam().getLogo(),
                fixture.getAwayTeam().getKoreanName()
        );
        FixtureOfLeagueResponse._Status status = new FixtureOfLeagueResponse._Status(
                liveStatus.getLongStatus(),
                liveStatus.getShortStatus(),
                liveStatus.getElapsed(),
                new FixtureOfLeagueResponse._Score(
                        liveStatus.getHomeScore(),
                        liveStatus.getAwayScore()
                )
        );

        return new FixtureOfLeagueResponse(
                fixture.getFixtureId(),
                match,
                home,
                away,
                status,
                fixture.isAvailable()
        );
    }

    public static FixtureInfoResponse toFixtureInfoResponse(Fixture fixture) {
        OffsetDateTime offsetDateTime = TimeConverter.toOffsetDateTime(fixture.getDate(), fixture.getTimezone());
        String dateStr = offsetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        _League league = new _League(
                fixture.getLeague().getLeagueId(),
                fixture.getLeague().getName(),
                fixture.getLeague().getKoreanName(),
                fixture.getLeague().getLogo()
        );
        _Team home = new _Team(
                fixture.getHomeTeam().getId(),
                fixture.getHomeTeam().getName(),
                fixture.getHomeTeam().getKoreanName(),
                fixture.getHomeTeam().getLogo()
        );
        _Team away = new _Team(
                fixture.getAwayTeam().getId(),
                fixture.getAwayTeam().getName(),
                fixture.getAwayTeam().getKoreanName(),
                fixture.getAwayTeam().getLogo()
        );
        return new FixtureInfoResponse(
                fixture.getFixtureId(),
                fixture.getReferee(),
                dateStr,
                league,
                home,
                away
        );
    }

    public static FixtureEventsResponse toFixtureEventsResponse(long fixtureId, List<FixtureEvent> events) {
        List<FixtureEventsResponse._Events> eventsList = new ArrayList<>();
        for (FixtureEvent event : events) {
            MatchPlayer eventPlayer = event.getPlayer();
            MatchPlayer eventAssist = event.getAssist();

            FixtureEventsResponse._Player respPlayer = createEventResponsePerson(eventPlayer);
            FixtureEventsResponse._Player respAssist = createEventResponsePerson(eventAssist);
            FixtureEventsResponse._Team respTeam = createEventResponseTeam(event);

            FixtureEventsResponse._Events _event = createEventResponse(event, respTeam, respPlayer, respAssist);
            eventsList.add(_event);
        }
        return new FixtureEventsResponse(fixtureId, eventsList);
    }

    private static FixtureEventsResponse._Events createEventResponse(FixtureEvent event, FixtureEventsResponse._Team respTeam, FixtureEventsResponse._Player respPlayer, FixtureEventsResponse._Player respAssist) {
        return new FixtureEventsResponse._Events(
                event.getSequence(),
                event.getTimeElapsed(),
                event.getExtraTime(),
                respTeam,
                respPlayer,
                respAssist,
                event.getType().toString(),
                event.getDetail(),
                event.getComments()
        );
    }

    private static FixtureEventsResponse._Team createEventResponseTeam(FixtureEvent event) {
        return new FixtureEventsResponse._Team(
                event.getTeam().getId(),
                event.getTeam().getName(),
                event.getTeam().getKoreanName()
        );
    }

    private static FixtureEventsResponse._Player createEventResponsePerson(MatchPlayer eventPlayer) {
        if (eventPlayer == null) {
            return null;
        }

        FixtureEventsResponse._Player respPlayer = null;
        if (isUnregisteredPlayer(eventPlayer)) {
            log.info("미등록 선수 정보가 존재합니다. {}", eventPlayer);
            respPlayer = new FixtureEventsResponse._Player(
                    null,
                    eventPlayer.getUnregisteredPlayerName(),
                    "",
                    eventPlayer.getUnregisteredPlayerNumber() == null ? 0 : eventPlayer.getUnregisteredPlayerNumber(),
                    eventPlayer.getTemporaryId() != null ? eventPlayer.getTemporaryId().toString() : null
            );
        } else {
            respPlayer = new FixtureEventsResponse._Player(
                    eventPlayer.getPlayer().getId(),
                    eventPlayer.getPlayer().getName(),
                    eventPlayer.getPlayer().getKoreanName(),
                    eventPlayer.getPlayer().getNumber(),
                    null
            );
        }
        return respPlayer;
    }

    public static FixtureLiveStatusResponse toFixtureLiveStatusResponse(long fixtureId, LiveStatus liveStatus) {
        FixtureLiveStatusResponse._Score score = new FixtureLiveStatusResponse._Score(
                liveStatus.getHomeScore(),
                liveStatus.getAwayScore()
        );
        return new FixtureLiveStatusResponse(
                fixtureId,
                new FixtureLiveStatusResponse._LiveStatus(
                        liveStatus.getElapsed(),
                        liveStatus.getShortStatus(),
                        liveStatus.getLongStatus(),
                        score
                )
        );
    }

    // TODO : [TEST] FixtureLineupResponse 맵핑에서 미등록 선수 포함 시 테스트 필요
    /**
     * Fixture -> Lineup -> MatchLineup -> MatchPlayer
     *
     * @param fixture
     * @return
     */
    public static FixtureLineupResponse toFixtureLineupResponse(Fixture fixture) {
        _Lineup lineup = null;
        if (fixture.getLineups() != null && !fixture.getLineups().isEmpty()) {
            try {
                List<MatchLineup> lineups = fixture.getLineups();
                final long homeTeamId = fixture.getHomeTeam().getId();

                MatchLineup findHomeLineup = lineups.stream()
                        .filter(l -> l.getTeam().getId() == homeTeamId).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("홈팀 라인업이 존재하지 않습니다."));
                MatchLineup findAwayLineup = lineups.stream()
                        .filter(l -> l.getTeam().getId() != homeTeamId).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("어웨이팀 라인업이 존재하지 않습니다."));

                List<MatchPlayer> findHomePlayers = sortWithStartLineupComparator(findHomeLineup);
                List<FixtureLineupResponse._LineupPlayer> homeStartXI = new ArrayList<>();
                List<FixtureLineupResponse._LineupPlayer> homeSubstitutes = new ArrayList<>();
                toLineupPlayerList(findHomePlayers, homeStartXI, homeSubstitutes);

                List<MatchPlayer> findAwayPlayers = sortWithStartLineupComparator(findAwayLineup);
                List<FixtureLineupResponse._LineupPlayer> awayStartXI = new ArrayList<>();
                List<FixtureLineupResponse._LineupPlayer> awaySubstitutes = new ArrayList<>();
                toLineupPlayerList(findAwayPlayers, awayStartXI, awaySubstitutes);

                log.info("findHomePlayers: {}", findHomePlayers);
                log.info("findAwayPlayers: {}", findAwayPlayers);

                Team homeTeam = findHomeLineup.getTeam();
                Team awayTeam = findAwayLineup.getTeam();

                FixtureLineupResponse._StartLineup homeLineup = new FixtureLineupResponse._StartLineup(
                        homeTeam.getId(),
                        homeTeam.getName(),
                        homeTeam.getKoreanName(),
                        findHomeLineup.getFormation(),
                        homeStartXI,
                        homeSubstitutes
                );
                FixtureLineupResponse._StartLineup awayLineup = new FixtureLineupResponse._StartLineup(
                        awayTeam.getId(),
                        awayTeam.getName(),
                        awayTeam.getKoreanName(),
                        findAwayLineup.getFormation(),
                        awayStartXI,
                        awaySubstitutes
                );

                lineup = new _Lineup(homeLineup, awayLineup);
            } catch (Exception e) {
                log.error("라인업 Response Mapping 중 오류 발생 : {}", e.getMessage(), e);
            }
        }
        return new FixtureLineupResponse(
                fixture.getFixtureId(),
                lineup
        );
    }

    private static @NotNull List<MatchPlayer> sortWithStartLineupComparator(MatchLineup findHomeLineup) {
        return findHomeLineup.getMatchPlayers().stream().sorted(new StartLineupComparator()).toList();
    }

    public static List<TeamsOfLeagueResponse> toTeamsOfLeagueResponseList(List<Team> teamsOfLeague) {
        List<TeamsOfLeagueResponse> responseList = new ArrayList<>();
        for (Team team : teamsOfLeague) {
            TeamsOfLeagueResponse response = new TeamsOfLeagueResponse(
                    team.getId(),
                    team.getName(),
                    team.getKoreanName(),
                    team.getLogo()
            );
            responseList.add(response);
        }
        return responseList;
    }

    private static void toLineupPlayerList(
            List<MatchPlayer> findAwayPlayers,
            List<FixtureLineupResponse._LineupPlayer> awayStartXI,
            List<FixtureLineupResponse._LineupPlayer> awaySubstitutes
    ) {
        for (MatchPlayer findAwayPlayer : findAwayPlayers) {
            FixtureLineupResponse._LineupPlayer responsePlayer
                    = lineupDataToResponseDtoElement(findAwayPlayer);

            if (responsePlayer.substitute()) {
                awaySubstitutes.add(responsePlayer);
            } else {
                awayStartXI.add(responsePlayer);
            }
        }
    }

    private static FixtureLineupResponse._LineupPlayer lineupDataToResponseDtoElement(MatchPlayer findAwayPlayer) {
        if(isUnregisteredPlayer(findAwayPlayer)) {
            return new FixtureLineupResponse._LineupPlayer(
                    0,
                    "",
                    findAwayPlayer.getUnregisteredPlayerName(),
                    findAwayPlayer.getUnregisteredPlayerNumber(),
                    MatchPlayer.UNREGISTERED_PLAYER_PHOTO_URL,
                    findAwayPlayer.getPosition(),
                    findAwayPlayer.getGrid(),
                    findAwayPlayer.getSubstitute(),
                    findAwayPlayer.getTemporaryId() != null ? findAwayPlayer.getTemporaryId().toString() : ""
            );
        } else {
            assert findAwayPlayer.getPlayer() != null;
            return new FixtureLineupResponse._LineupPlayer(
                    findAwayPlayer.getPlayer().getId(),
                    findAwayPlayer.getPlayer().getKoreanName(),
                    findAwayPlayer.getPlayer().getName(),
                    findAwayPlayer.getPlayer().getNumber(),
                    findAwayPlayer.getPlayer().getPhotoUrl(),
                    findAwayPlayer.getPosition(),
                    findAwayPlayer.getGrid(),
                    findAwayPlayer.getSubstitute(),
                    findAwayPlayer.getTemporaryId() != null ? findAwayPlayer.getTemporaryId().toString() : ""
            );
        }
    }

    private static boolean isUnregisteredPlayer(MatchPlayer mp) {
        return mp.getPlayer() == null;
    }

}
