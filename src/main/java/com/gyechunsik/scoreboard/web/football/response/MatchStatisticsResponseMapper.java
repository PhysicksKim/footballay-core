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

        List<PlayerStatistics> homePlayerStats = matchStat.getHomePlayerStatistics();
        List<PlayerStatistics> awayPlayerStats = matchStat.getAwayPlayerStatistics();

        // RESPONSE records
        MatchStatisticsResponse._ResponseFixture responseFixture = toResponseFixture(fixture, liveStatus);
        MatchStatisticsResponse._ResponseTeam homeTeam = toResponseTeam(home);
        MatchStatisticsResponse._ResponseTeam awayTeam = toResponseTeam(away);

        MatchStatisticsResponse._ResponseTeamStatistics homeTeamStat = toTeamStatisticsResponse(matchStat.getHomeStatistics());
        MatchStatisticsResponse._ResponseTeamStatistics awayTeamStat = toTeamStatisticsResponse(matchStat.getAwayStatistics());
        List<MatchStatisticsResponse._ResponsePlayerStatistics> homePlayerStatList = toResponsePlayerStatisticsList(homePlayerStats);
        List<MatchStatisticsResponse._ResponsePlayerStatistics> awayPlayerStatList = toResponsePlayerStatisticsList(awayPlayerStats);

        // end RESPONSE record
        return new MatchStatisticsResponse(
                responseFixture,
                new MatchStatisticsResponse._ResponseTeamWithStatistics(homeTeam, homeTeamStat, homePlayerStatList),
                new MatchStatisticsResponse._ResponseTeamWithStatistics(awayTeam, awayTeamStat, awayPlayerStatList)
        );
    }

    private static List<MatchStatisticsResponse._XG> toXGList(List<ExpectedGoals> xgList) {
        return xgList.stream()
                .map(xg -> new MatchStatisticsResponse._XG(
                        safeInt(xg.getElapsed()),
                        xg.getXg()
                ))
                .toList();
    }

    private static MatchStatisticsResponse._ResponseFixture toResponseFixture(Fixture fixture, LiveStatus liveStatus) {
        return new MatchStatisticsResponse._ResponseFixture(
                fixture.getFixtureId(),
                liveStatus.getElapsed() != null ? liveStatus.getElapsed() : 0,
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

    private static MatchStatisticsResponse._ResponseTeamStatistics toTeamStatisticsResponse(TeamStatistics teamStat) {
        // null 체크를 통해 기본값 반환
        if (teamStat == null) {
            log.info("Team statistics not available, returning empty statistics.");
            return new MatchStatisticsResponse._ResponseTeamStatistics(
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,  List.of() // 기본값으로 빈 통계 제공
            );
        }

        List<ExpectedGoals> expectedGoalsList = teamStat.getExpectedGoalsList() != null ? teamStat.getExpectedGoalsList() : List.of();
        List<MatchStatisticsResponse._XG> xgList = toXGList(expectedGoalsList);

        return createTeamStatisticsRecord(teamStat, xgList);
    }

    private static MatchStatisticsResponse._ResponseTeamStatistics createTeamStatisticsRecord(
            TeamStatistics teamStatistics,
            List<MatchStatisticsResponse._XG> xgList) {
        return new MatchStatisticsResponse._ResponseTeamStatistics(
                safeInt(teamStatistics.getShotsOnGoal()),
                safeInt(teamStatistics.getShotsOffGoal()),
                safeInt(teamStatistics.getTotalShots()),
                safeInt(teamStatistics.getBlockedShots()),
                safeInt(teamStatistics.getShotsInsideBox()),
                safeInt(teamStatistics.getShotsOutsideBox()),
                safeInt(teamStatistics.getFouls()),
                safeInt(teamStatistics.getCornerKicks()),
                safeInt(teamStatistics.getOffsides()),
                safeInt(teamStatistics.getBallPossession()),
                safeInt(teamStatistics.getYellowCards()),
                safeInt(teamStatistics.getRedCards()),
                safeInt(teamStatistics.getGoalkeeperSaves()),
                safeInt(teamStatistics.getTotalPasses()),
                safeInt(teamStatistics.getPassesAccurate()),
                safeInt(teamStatistics.getPassesAccuracyPercentage()),
                safeInt(teamStatistics.getGoalsPrevented()),
                xgList
        );
    }

    private static MatchStatisticsResponse._ResponsePlayerStatistics toResponsePlayerStatistics(PlayerStatistics playerStat) {
        MatchStatisticsResponse._PlayerInfoBasic playerInfoBasic = new MatchStatisticsResponse._PlayerInfoBasic(
                playerStat.getPlayer().getId(),
                playerStat.getPlayer().getName(),
                playerStat.getPlayer().getKoreanName(),
                playerStat.getPlayer().getPhotoUrl(),
                playerStat.getPosition(),
                playerStat.getPlayer().getNumber()
        );

        MatchStatisticsResponse._PlayerStatistics playerStatistics = new MatchStatisticsResponse._PlayerStatistics(
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
                safeInt(playerStat.getPassesAccuracy()),
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

        return new MatchStatisticsResponse._ResponsePlayerStatistics(
                playerInfoBasic,
                playerStatistics
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
