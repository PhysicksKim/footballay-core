package com.gyechunsik.scoreboard.web.admin.football.dto;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus;

public class FootballDtoMapper {

    public static LiveStatusDto toLiveStatusDto(LiveStatus liveStatus) {
        return new LiveStatusDto(
                liveStatus.getLongStatus(),
                liveStatus.getShortStatus(),
                liveStatus.getElapsed()
        );
    }

    public static LeagueDto toLeagueDto(League league) {
        return new LeagueDto(
                league.getLeagueId(),
                league.getName(),
                league.getKoreanName(),
                league.getLogo()
        );
    }

    public static TeamDto toTeamDto(Team team) {
        return new TeamDto(
                team.getId(),
                team.getName(),
                team.getKoreanName(),
                team.getLogo()
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
                fixture.getDate(),
                fixture.getTimestamp(),
                fixture.isAvailable(),
                toLiveStatusDto(fixture.getLiveStatus()),
                toLeagueDto(fixture.getLeague()),
                toTeamDto(fixture.getHomeTeam()),
                toTeamDto(fixture.getAwayTeam())
        );
    }
}
