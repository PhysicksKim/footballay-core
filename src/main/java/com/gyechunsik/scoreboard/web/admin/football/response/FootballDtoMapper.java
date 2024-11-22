package com.gyechunsik.scoreboard.web.admin.football.response;

import com.gyechunsik.scoreboard.domain.football.dto.*;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.LiveStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class FootballDtoMapper {

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
                fixture.date(),
                fixture.timestamp(),
                toLiveStatusDto(fixture.liveStatus()),
                toLeagueDto(fixture.league()),
                toTeamResponse(fixture.homeTeam()),
                toTeamResponse(fixture.awayTeam())
        );
    }

    public static AvailableFixtureDto toAvailableFixtureDto(FixtureInfoDto fixtureInfoDto) {
        return new AvailableFixtureDto(
                fixtureInfoDto.fixtureId(),
                fixtureInfoDto.referee(),
                fixtureInfoDto.timezone(),
                fixtureInfoDto.date(),
                fixtureInfoDto.timestamp(),
                fixtureInfoDto.available(),
                toLiveStatusDto(fixtureInfoDto.liveStatus()),
                toLeagueDto(fixtureInfoDto.league()),
                toTeamResponse(fixtureInfoDto.homeTeam()),
                toTeamResponse(fixtureInfoDto.awayTeam())
        );
    }

    public static AvailableLeagueDto toAvailableLeagueDto(LeagueDto league) {
        return new AvailableLeagueDto(
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
}
