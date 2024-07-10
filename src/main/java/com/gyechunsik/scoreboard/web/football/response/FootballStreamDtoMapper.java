package com.gyechunsik.scoreboard.web.football.response;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartLineup;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartPlayer;
import com.gyechunsik.scoreboard.utils.TimeConverter;
import com.gyechunsik.scoreboard.web.football.response.fixture.info.FixtureInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.gyechunsik.scoreboard.web.football.response.fixture.info.FixtureInfoResponse.*;

public class FootballStreamDtoMapper {

    private static final Logger log = LoggerFactory.getLogger(FootballStreamDtoMapper.class);

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
        final String homeTeamName = fixture.getHomeTeam().getKoreanName() != null ?
                fixture.getHomeTeam().getKoreanName() : fixture.getHomeTeam().getName();
        final String awayTeamName = fixture.getAwayTeam().getKoreanName() != null ?
                fixture.getAwayTeam().getKoreanName() : fixture.getAwayTeam().getName();
        return new FixtureOfLeagueResponse(
                fixture.getFixtureId(),
                fixture.getDate().toString(),
                fixture.getLiveStatus().getShortStatus(),
                homeTeamName,
                awayTeamName
        );
    }

    public static FixtureInfoResponse toFixtureInfoResponse(Fixture fixture) {
        OffsetDateTime offsetDateTime = TimeConverter.toOffsetDateTime(fixture.getDate(), fixture.getTimezone());
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

        _LiveStatus liveStatus = new _LiveStatus(
                fixture.getLiveStatus().getElapsed(),
                fixture.getLiveStatus().getShortStatus(),
                fixture.getLiveStatus().getLongStatus()
        );

        List<_FixtureEventResponse> events = new ArrayList<>();
        List<FixtureEvent> findEvents = fixture.getEvents();
        for (FixtureEvent findEvent : findEvents) {
            _Player player = new _Player(
                    findEvent.getPlayer().getId(),
                    findEvent.getPlayer().getName(),
                    findEvent.getPlayer().getKoreanName(),
                    findEvent.getPlayer().getPhotoUrl()
            );
            _Player assist = null;
            if (findEvent.getAssist() != null) {
                assist = new _Player(
                        findEvent.getAssist().getId(),
                        findEvent.getAssist().getName(),
                        findEvent.getAssist().getKoreanName(),
                        findEvent.getAssist().getPhotoUrl()
                );
            }

            _FixtureEventResponse event = new _FixtureEventResponse(
                    findEvent.getTeam().getId(),
                    player,
                    assist,
                    findEvent.getTimeElapsed(),
                    findEvent.getType().toString(),
                    findEvent.getDetail()
            );
            events.add(event);
        }

        // _Lineup -> _StartLineup -> _StartPlayer
        _Lineup lineup = null;
        if (fixture.getLineups() != null) {
            try {
                List<StartLineup> lineups = fixture.getLineups();
                final long homeTeamId = fixture.getHomeTeam().getId();

                StartLineup findHomeLineup = lineups.stream()
                        .filter(l -> l.getTeam().getId() == homeTeamId).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("홈팀 라인업이 존재하지 않습니다."));
                StartLineup findAwayLineup = lineups.stream()
                        .filter(l -> l.getTeam().getId() != homeTeamId).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("어웨이팀 라인업이 존재하지 않습니다."));

                List<StartPlayer> findHomePlayers = findHomeLineup.getStartPlayers();
                List<_StartPlayer> homeStartXI = new ArrayList<>();
                List<_StartPlayer> homeSubstitutes = new ArrayList<>();
                toLineupPlayerList(findHomePlayers, homeStartXI, homeSubstitutes);

                List<StartPlayer> findAwayPlayers = findAwayLineup.getStartPlayers();
                List<_StartPlayer> awayStartXI = new ArrayList<>();
                List<_StartPlayer> awaySubstitutes = new ArrayList<>();
                toLineupPlayerList(findAwayPlayers, awayStartXI, awaySubstitutes);

                LineupComparator comparator = new LineupComparator();
                homeStartXI.sort(comparator);
                homeSubstitutes.sort(comparator);
                awayStartXI.sort(comparator);
                awaySubstitutes.sort(comparator);

                _StartLineup homeLineup = new _StartLineup(
                        findHomeLineup.getTeam().getId(),
                        findHomeLineup.getFormation(),
                        homeStartXI,
                        homeSubstitutes
                );
                _StartLineup awayLineup = new _StartLineup(
                        findAwayLineup.getTeam().getId(),
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

        FixtureInfoResponse response = new FixtureInfoResponse(
                fixture.getFixtureId(),
                fixture.getReferee(),
                offsetDateTime.toString(),
                liveStatus,
                league,
                home,
                away,
                events,
                lineup
        );

        return response;
    }

    private static void toLineupPlayerList(List<StartPlayer> findAwayPlayers, List<_StartPlayer> awayStartXI, List<_StartPlayer> awaySubstitutes) {
        for (StartPlayer findAwayPlayer : findAwayPlayers) {
            _StartPlayer awayPlayer = new _StartPlayer(
                    findAwayPlayer.getPlayer().getId(),
                    findAwayPlayer.getPlayer().getKoreanName(),
                    findAwayPlayer.getPlayer().getName(),
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

    private static class LineupComparator implements Comparator<_StartPlayer> {
        @Override
        public int compare(_StartPlayer target, _StartPlayer reference) {
            boolean targetIsSub = target.substitute();
            boolean referenceIsSub = reference.substitute();

            if (!targetIsSub && referenceIsSub) return -1;
            if (targetIsSub && !referenceIsSub) return 1;

            if (targetIsSub && referenceIsSub) return Long.compare(target.id(), reference.id());

            String[] grid1 = target.grid().split(":");
            String[] grid2 = reference.grid().split(":");
            int xCompare = Integer.compare(Integer.parseInt(grid1[0]), Integer.parseInt(grid2[0]));
            return xCompare != 0 ? xCompare : Integer.compare(Integer.parseInt(grid1[1]), Integer.parseInt(grid2[1]));
        }
    }

}
