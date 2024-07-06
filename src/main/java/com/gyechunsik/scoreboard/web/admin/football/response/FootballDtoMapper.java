package com.gyechunsik.scoreboard.web.admin.football.response;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class FootballDtoMapper {

    public static LiveStatusResponse toLiveStatusDto(LiveStatus liveStatus) {
        return new LiveStatusResponse(
                liveStatus.getLongStatus(),
                liveStatus.getShortStatus(),
                liveStatus.getElapsed()
        );
    }

    public static LeagueResponse toLeagueDto(League league) {
        return new LeagueResponse(
                league.getLeagueId(),
                league.getName(),
                league.getKoreanName(),
                league.getLogo()
        );
    }

    public static TeamResponse toTeamDto(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getKoreanName(),
                team.getLogo()
        );
    }

    public static PlayerResponse toPlayerDto(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getKoreanName(),
                player.getPhotoUrl(),
                player.getPosition()
        );
    }

    public static FixtureResponse toFixtureDto(Fixture fixture) {
        return new FixtureResponse(
                fixture.getFixtureId(),
                fixture.getReferee(),
                fixture.getTimezone(),
                ZonedDateTime.of(fixture.getDate(), ZoneId.of(fixture.getTimezone())),
                fixture.getTimestamp(),
                toLiveStatusDto(fixture.getLiveStatus()),
                toLeagueDto(fixture.getLeague()),
                toTeamDto(fixture.getHomeTeam()),
                toTeamDto(fixture.getAwayTeam())
        );
    }

    public static AvailableLeagueDto toAvailableLeagueDto(League league) {
        return new AvailableLeagueDto(
                league.getLeagueId(),
                league.getName(),
                league.getKoreanName(),
                league.getLogo(),
                league.isAvailable(),
                league.getCurrentSeason()
        );
    }

    public static AvailableFixtureDto toAvailableFixtureDto(Fixture fixture) {
        return new AvailableFixtureDto(
                fixture.getFixtureId(),
                fixture.getReferee(),
                fixture.getTimezone(),
                ZonedDateTime.of(fixture.getDate(), ZoneId.of(fixture.getTimezone())),
                fixture.getTimestamp(),
                fixture.isAvailable(),
                toLiveStatusDto(fixture.getLiveStatus()),
                toLeagueDto(fixture.getLeague()),
                toTeamDto(fixture.getHomeTeam()),
                toTeamDto(fixture.getAwayTeam())
        );
    }

}
