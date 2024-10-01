package com.gyechunsik.scoreboard.web.football.response;

import com.gyechunsik.scoreboard.domain.football.model.MatchStatistics;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.ExpectedGoals;
import com.gyechunsik.scoreboard.domain.football.persistence.live.LiveStatus;
import com.gyechunsik.scoreboard.domain.football.persistence.live.PlayerStatistics;
import com.gyechunsik.scoreboard.domain.football.persistence.live.TeamStatistics;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MatchStatisticsResponseMapper {

    public static MatchStatisticsResponse toResponse(MatchStatistics matchStat) {
        Fixture fixture = matchStat.getFixture();
        LiveStatus liveStatus = matchStat.getLiveStatus();
        Team home = matchStat.getHome();
        Team away = matchStat.getAway();
        TeamStatistics homeStat = matchStat.getHomeStatistics();
        TeamStatistics awayStat = matchStat.getAwayStatistics();
        List<ExpectedGoals> homeExpectedGoalsList = homeStat.getExpectedGoalsList();
        List<ExpectedGoals> awayExpectedGoalsList = awayStat.getExpectedGoalsList();
        List<PlayerStatistics> homePlayerStats = matchStat.getHomePlayerStatistics();
        List<PlayerStatistics> awayPlayerStats = matchStat.getAwayPlayerStatistics();

        // RESPONSE records
        MatchStatisticsResponse._ResponseFixture responseFixture = toResponseFixture(fixture, liveStatus);
        MatchStatisticsResponse._ResponseTeam homeTeam = toResponseTeam(home);
        MatchStatisticsResponse._ResponseTeam awayTeam = toResponseTeam(away);
        MatchStatisticsResponse._ResponseTeamStatistics homeTeamStat = toResponseTeamStatistics(homeStat, toXGList(homeExpectedGoalsList));
        MatchStatisticsResponse._ResponseTeamStatistics awayTeamStat = toResponseTeamStatistics(awayStat, toXGList(awayExpectedGoalsList));
        List<MatchStatisticsResponse._ResponsePlayerStatistics> homePlayerStatList = toResponsePlayerStatisticsList(homePlayerStats);
        List<MatchStatisticsResponse._ResponsePlayerStatistics> awayPlayerStatList = toResponsePlayerStatisticsList(awayPlayerStats);

        // end RESPONSE record
        MatchStatisticsResponse response = new MatchStatisticsResponse(
                responseFixture,
                new MatchStatisticsResponse._ResponseTeamWithStatistics(homeTeam, homeTeamStat, homePlayerStatList),
                new MatchStatisticsResponse._ResponseTeamWithStatistics(awayTeam, awayTeamStat, awayPlayerStatList)
        );
        return response;
    }


    private static List<MatchStatisticsResponse._XG> toXGList(List<ExpectedGoals> xgList) {
        return xgList.stream()
                .map(xg -> new MatchStatisticsResponse._XG(
                        xg.getElapsed(),
                        xg.getXg()
                ))
                .toList();
    }

    private static MatchStatisticsResponse._ResponseFixture toResponseFixture(Fixture fixture, LiveStatus liveStatus) {
        return new MatchStatisticsResponse._ResponseFixture(
                fixture.getFixtureId(),
                liveStatus.getElapsed(),
                liveStatus.getShortStatus()
        );
    }

    private static MatchStatisticsResponse._ResponseTeam toResponseTeam(Team team) {
        return new MatchStatisticsResponse._ResponseTeam(
                team.getId(),
                team.getName(),
                team.getKoreanName(),
                team.getLogo()
        );
    }

    private static MatchStatisticsResponse._ResponseTeamStatistics toResponseTeamStatistics(
            TeamStatistics teamStatistics,
            List<MatchStatisticsResponse._XG> xgList) {
        return new MatchStatisticsResponse._ResponseTeamStatistics(
                teamStatistics.getShotsOnGoal(),
                teamStatistics.getShotsOffGoal(),
                teamStatistics.getTotalShots(),
                teamStatistics.getBlockedShots(),
                teamStatistics.getShotsInsideBox(),
                teamStatistics.getShotsOutsideBox(),
                teamStatistics.getFouls(),
                teamStatistics.getCornerKicks(),
                teamStatistics.getOffsides(),
                teamStatistics.getBallPossession(),
                teamStatistics.getYellowCards(),
                teamStatistics.getRedCards(),
                teamStatistics.getGoalkeeperSaves(),
                teamStatistics.getTotalPasses(),
                teamStatistics.getPassesAccurate(),
                teamStatistics.getPassesAccuracyPercentage(),
                teamStatistics.getGoalsPrevented(),
                xgList
        );
    }

    private static MatchStatisticsResponse._ResponsePlayerStatistics toResponsePlayerStatistics(PlayerStatistics playerStat) {
        return new MatchStatisticsResponse._ResponsePlayerStatistics(
                playerStat.getId(),
                playerStat.getPlayer().getName(),
                playerStat.getPlayer().getKoreanName(),
                safeInt(playerStat.getMinutesPlayed()),
                playerStat.getPosition(),
                playerStat.getRating(),
                playerStat.getCaptain(),
                playerStat.getSubstitute(),
                safeInt(playerStat.getShotsTotal()),
                safeInt(playerStat.getShotsOn()),
                safeInt(playerStat.getGoals()),
                safeInt(playerStat.getGoalsConceded()),
                safeInt(playerStat.getAssists()),
                safeInt(playerStat.getSaves()),
                safeInt(playerStat.getPassesTotal()),
                safeInt(playerStat.getPassesKey()),
                playerStat.getPassesAccuracy(),
                safeInt(playerStat.getTacklesTotal()),
                safeInt(playerStat.getInterceptions()),
                safeInt(playerStat.getDuelsTotal()),
                safeInt(playerStat.getDuelsWon()),
                safeInt(playerStat.getDribblesAttempts()),
                safeInt(playerStat.getDribblesSuccess()),
                safeInt(playerStat.getFoulsCommitted()),
                safeInt(playerStat.getFoulsDrawn()),
                safeInt(playerStat.getYellowCards()),
                safeInt(playerStat.getRedCards()),
                safeInt(playerStat.getPenaltiesScored()),
                safeInt(playerStat.getPenaltiesMissed()),
                safeInt(playerStat.getPenaltiesSaved())
        );
    }

    private static List<MatchStatisticsResponse._ResponsePlayerStatistics> toResponsePlayerStatisticsList(List<PlayerStatistics> playerStats) {
        return playerStats.stream()
                .map(MatchStatisticsResponseMapper::toResponsePlayerStatistics)
                .toList();
    }

    private static int safeInt(Integer value) {
        return value != null ? value : 0;
    }
}
