package com.gyechunsik.scoreboard.web.admin.football.response.mapper;

import com.gyechunsik.scoreboard.domain.football.dto.*;
import com.gyechunsik.scoreboard.web.admin.football.response.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FootballDtoMapper {

    public static ExternalApiStatusResponse toExternalApiStatusDto(ExternalApiStatusDto externalApiStatusDto) {
        return new ExternalApiStatusResponse(
                externalApiStatusDto.current(),
                externalApiStatusDto.minuteLimit(),
                externalApiStatusDto.minuteRemaining(),
                externalApiStatusDto.dayLimit(),
                externalApiStatusDto.dayRemaining(),
                externalApiStatusDto.active()
        );
    }

    public static LiveStatusResponse toLiveStatusDto(LiveStatusDto liveStatusDto) {
        return new LiveStatusResponse(
                liveStatusDto.longStatus(),
                liveStatusDto.shortStatus(),
                liveStatusDto.elapsed()
        );
    }

    public static LeagueResponse toLeagueDto(LeagueDto league) {
        return new LeagueResponse(
                league.leagueId(),
                league.name(),
                league.koreanName(),
                league.logo()
        );
    }

    public static TeamResponse toTeamResponse(TeamDto team) {
        return new TeamResponse(
                team.id(),
                team.name(),
                team.koreanName(),
                team.logo()
        );
    }

    public static PlayerResponse toPlayerDto(PlayerDto player) {
        return new PlayerResponse(
                player.id(),
                player.name(),
                player.koreanName(),
                player.photoUrl(),
                player.position()
        );
    }

    public static FixtureResponse toFixtureDto(FixtureInfoDto fixture) {
        return new FixtureResponse(
                fixture.fixtureId(),
                fixture.referee(),
                fixture.timezone(),
                formatZonedDateTime(fixture.date()),
                fixture.timestamp(),
                toLiveStatusDto(fixture.liveStatus()),
                toLeagueDto(fixture.league()),
                toTeamResponse(fixture.homeTeam()),
                toTeamResponse(fixture.awayTeam()),
                fixture.available()
        );
    }

    public static AvailableFixtureResponse toAvailableFixtureDto(FixtureInfoDto fixtureInfoDto) {
        return new AvailableFixtureResponse(
                fixtureInfoDto.fixtureId(),
                fixtureInfoDto.referee(),
                fixtureInfoDto.timezone(),
                formatZonedDateTime(fixtureInfoDto.date()), // 여기 date() 가 ZonedDateTime 이라서 String 으로 바꿔줘야 함
                fixtureInfoDto.timestamp(),
                fixtureInfoDto.available(),
                toLiveStatusDto(fixtureInfoDto.liveStatus()),
                toLeagueDto(fixtureInfoDto.league()),
                toTeamResponse(fixtureInfoDto.homeTeam()),
                toTeamResponse(fixtureInfoDto.awayTeam())
        );
    }

    public static AvailableLeagueResponse toAvailableLeagueDto(LeagueDto league) {
        return new AvailableLeagueResponse(
                league.leagueId(),
                league.name(),
                league.koreanName(),
                league.logo(),
                league.available(),
                league.currentSeason()
        );
    }

    public static TeamsOfPlayerResponse toTeamsOfPlayer(PlayerDto player, List<TeamDto> teams) {
        TeamsOfPlayerResponse._Player _player = new TeamsOfPlayerResponse._Player(
                player.id(),
                player.name(),
                player.koreanName(),
                player.photoUrl(),
                player.position()
        );
        TeamsOfPlayerResponse._Team[] _teams = teams.stream()
                .map(team -> new TeamsOfPlayerResponse._Team(
                        team.id(),
                        team.name(),
                        team.koreanName(),
                        team.logo()
                ))
                .toArray(TeamsOfPlayerResponse._Team[]::new);
        return new TeamsOfPlayerResponse(_player, _teams);
    }

    private static String formatZonedDateTime(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) return null;
        return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
