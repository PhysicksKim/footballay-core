package com.gyechunsik.scoreboard.web.football.response;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;

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



}
