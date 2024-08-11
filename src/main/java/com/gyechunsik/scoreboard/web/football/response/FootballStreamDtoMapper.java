package com.gyechunsik.scoreboard.web.football.response;

import com.gyechunsik.scoreboard.domain.football.comparator.StartLineupComparator;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartLineup;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartPlayer;
import com.gyechunsik.scoreboard.utils.TimeConverter;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureEventsResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureInfoResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLineupResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLineupResponse._Lineup;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLiveStatusResponse;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.gyechunsik.scoreboard.web.football.response.fixture.FixtureInfoResponse.*;

// import static com.gyechunsik.scoreboard.web.football.response.fixture.FixtureInfoResponse.*;

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
        if(fixture.getHomeTeam() == null || fixture.getAwayTeam() == null) {
            throw new IllegalArgumentException("홈팀 또는 어웨이팀 정보가 존재하지 않습니다. homeTeamIsNull:" + (fixture.getHomeTeam()==null) + ", awayTeamIsNull:" + (fixture.getAwayTeam()==null));
        }
        if(fixture.getLiveStatus() == null) {
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
            FixtureEventsResponse._Events _event = new FixtureEventsResponse._Events(
                    event.getSequence(),
                    event.getTimeElapsed(),
                    event.getExtraTime(),
                    new FixtureEventsResponse._Team(
                            event.getTeam().getId(),
                            event.getTeam().getName(),
                            event.getTeam().getKoreanName()
                    ),
                    new FixtureEventsResponse._Player(
                            event.getPlayer().getId(),
                            event.getPlayer().getName(),
                            event.getPlayer().getKoreanName(),
                            event.getPlayer().getNumber()
                    ),
                    event.getAssist() == null ? null : new FixtureEventsResponse._Player(
                            event.getAssist().getId(),
                            event.getAssist().getName(),
                            event.getAssist().getKoreanName(),
                            event.getAssist().getNumber()
                    ),
                    event.getType().toString(),
                    event.getDetail(),
                    event.getComments()
            );
            eventsList.add(_event);
        }
        return new FixtureEventsResponse(fixtureId, eventsList);
    }

    public static FixtureLiveStatusResponse toFixtureLiveStatusResponse(long fixtureId, LiveStatus liveStatus) {
        return new FixtureLiveStatusResponse(
                fixtureId,
                new FixtureLiveStatusResponse._LiveStatus(
                        liveStatus.getElapsed(),
                        liveStatus.getShortStatus(),
                        liveStatus.getLongStatus()
                )
        );
    }

    /**
     * Fixture -> Lineup -> StartLineup -> StartPlayer
     * @param fixture
     * @return
     */
    public static FixtureLineupResponse toFixtureLineupResponse(Fixture fixture) {
        _Lineup lineup = null;
        if (fixture.getLineups() != null && !fixture.getLineups().isEmpty()) {
            try {
                List<StartLineup> lineups = fixture.getLineups();
                final long homeTeamId = fixture.getHomeTeam().getId();

                StartLineup findHomeLineup = lineups.stream()
                        .filter(l -> l.getTeam().getId() == homeTeamId).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("홈팀 라인업이 존재하지 않습니다."));
                StartLineup findAwayLineup = lineups.stream()
                        .filter(l -> l.getTeam().getId() != homeTeamId).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("어웨이팀 라인업이 존재하지 않습니다."));

                List<StartPlayer> findHomePlayers = findHomeLineup.getStartPlayers().stream().sorted(new StartLineupComparator()).toList();
                List<FixtureLineupResponse._StartPlayer> homeStartXI = new ArrayList<>();
                List<FixtureLineupResponse._StartPlayer> homeSubstitutes = new ArrayList<>();
                toLineupPlayerList(findHomePlayers, homeStartXI, homeSubstitutes);

                List<StartPlayer> findAwayPlayers = findAwayLineup.getStartPlayers().stream().sorted(new StartLineupComparator()).toList();
                List<FixtureLineupResponse._StartPlayer> awayStartXI = new ArrayList<>();
                List<FixtureLineupResponse._StartPlayer> awaySubstitutes = new ArrayList<>();
                toLineupPlayerList(findAwayPlayers, awayStartXI, awaySubstitutes);

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

                lineup = new _Lineup(
                        homeLineup,
                        awayLineup
                );
            } catch (Exception e) {
                log.error("라인업 Response Mapping 중 오류 발생 : {}", e.getMessage(), e);
            }
        }
        return new FixtureLineupResponse(
                fixture.getFixtureId(),
                lineup
        );
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
            List<StartPlayer> findAwayPlayers,
                                           List<FixtureLineupResponse._StartPlayer> awayStartXI,
                                           List<FixtureLineupResponse._StartPlayer> awaySubstitutes
    ) {
        for (StartPlayer findAwayPlayer : findAwayPlayers) {
            FixtureLineupResponse._StartPlayer awayPlayer = new FixtureLineupResponse._StartPlayer(
                    findAwayPlayer.getPlayer().getId(),
                    findAwayPlayer.getPlayer().getKoreanName(),
                    findAwayPlayer.getPlayer().getName(),
                    findAwayPlayer.getPlayer().getNumber(),
                    findAwayPlayer.getPlayer().getPhotoUrl(),
                    findAwayPlayer.getPosition(),
                    findAwayPlayer.getGrid(),
                    findAwayPlayer.getSubstitute()
            );
            if (awayPlayer.substitute()) {
                awaySubstitutes.add(awayPlayer);
            } else {
                awayStartXI.add(awayPlayer);
            }
        }
    }
}
