package com.gyechunsik.scoreboard.web.football.response;

import com.gyechunsik.scoreboard.domain.football.dto.MatchStatisticsDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.gyechunsik.scoreboard.domain.football.dto.MatchStatisticsDto.*;

// TODO : 미등록 선수에 대한 통계 Response 생성 전략 필요
@Slf4j
public class MatchStatisticsResponseMapper {

    public static MatchStatisticsResponse toResponse(MatchStatisticsDto matchStat) {
        MatchStatsFixture fixture = matchStat.getFixture();
        MatchStatsLiveStatus liveStatus = matchStat.getLiveStatus();
        MatchStatsTeam home = matchStat.getHome();
        MatchStatsTeam away = matchStat.getAway();

        List<MatchStatsPlayers> homePlayerStats = matchStat.getHomePlayerStatistics();
        List<MatchStatsPlayers> awayPlayerStats = matchStat.getAwayPlayerStatistics();

        MatchStatisticsResponse._ResponseFixture responseFixture = toResponseFixture(fixture, liveStatus);
        MatchStatisticsResponse._ResponseTeam homeTeam = toResponseTeam(home);
        MatchStatisticsResponse._ResponseTeam awayTeam = toResponseTeam(away);

        MatchStatisticsResponse._ResponseTeamStatistics homeTeamStat = toTeamStatisticsResponse(matchStat.getHomeStatistics());
        MatchStatisticsResponse._ResponseTeamStatistics awayTeamStat = toTeamStatisticsResponse(matchStat.getAwayStatistics());
        List<MatchStatisticsResponse._ResponsePlayerStatistics> homePlayerStatList = toResponsePlayerStatisticsList(homePlayerStats);
        List<MatchStatisticsResponse._ResponsePlayerStatistics> awayPlayerStatList = toResponsePlayerStatisticsList(awayPlayerStats);

        return new MatchStatisticsResponse(
                responseFixture,
                new MatchStatisticsResponse._ResponseTeamWithStatistics(homeTeam, homeTeamStat, homePlayerStatList),
                new MatchStatisticsResponse._ResponseTeamWithStatistics(awayTeam, awayTeamStat, awayPlayerStatList)
        );
    }

    private static MatchStatisticsResponse._ResponseFixture toResponseFixture(MatchStatsFixture fixture, MatchStatsLiveStatus liveStatus) {
        return new MatchStatisticsResponse._ResponseFixture(
                fixture.getId(),
                liveStatus.getElapsed() != null ? liveStatus.getElapsed() : 0,
                liveStatus.getShortStatus()
        );
    }

    private static MatchStatisticsResponse._ResponseTeam toResponseTeam(MatchStatsTeam team) {
        return new MatchStatisticsResponse._ResponseTeam(
                team.getId(),
                team.getName(),
                team.getKoreanName(),
                team.getLogo()
        );
    }

    private static MatchStatisticsResponse._ResponseTeamStatistics toTeamStatisticsResponse(MatchStatsTeamStatistics teamStat) {
        // null 체크를 통해 기본값 반환
        if (teamStat == null) {
            log.info("Team statistics not available, returning empty statistics.");
            return new MatchStatisticsResponse._ResponseTeamStatistics(
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, List.of() // 기본값으로 빈 통계 제공
            );
        }

        List<MatchStatsXg> expectedGoalsList = teamStat.getExpectedGoalsList() != null ? teamStat.getExpectedGoalsList() : List.of();
        List<MatchStatisticsResponse._XG> xgList = toXGList(expectedGoalsList);

        return createTeamStatisticsRecord(teamStat, xgList);
    }

    private static List<MatchStatisticsResponse._ResponsePlayerStatistics> toResponsePlayerStatisticsList(List<MatchStatsPlayers> mpsDtoList) {
        return mpsDtoList.stream()
                .map(MatchStatisticsResponseMapper::toResponsePlayerStatistics)
                .toList();
    }

    private static List<MatchStatisticsResponse._XG> toXGList(List<MatchStatsXg> xgList) {
        return xgList.stream()
                .map(xg -> new MatchStatisticsResponse._XG(
                        safeInt(xg.getElapsed()),
                        xg.getXg()
                ))
                .toList();
    }

    private static MatchStatisticsResponse._ResponsePlayerStatistics toResponsePlayerStatistics(MatchStatsPlayers mpsDto) {
        MatchStatisticsResponse._PlayerInfoBasic playerInfoBasic = createResponsePlayerInfoBasic(mpsDto);

        MatchStatsPlayerStatistics playerStat = mpsDto.getStatistics();
        MatchStatisticsResponse._PlayerStatistics playerStatistics = createResponsePlayerStatistics(playerStat);

        return new MatchStatisticsResponse._ResponsePlayerStatistics(
                playerInfoBasic,
                playerStatistics
        );
    }

    private static MatchStatisticsResponse._ResponseTeamStatistics createTeamStatisticsRecord(
            MatchStatsTeamStatistics teamStatistics,
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

    private static MatchStatisticsResponse._PlayerStatistics createResponsePlayerStatistics(MatchStatsPlayerStatistics playerStat) {
        return new MatchStatisticsResponse._PlayerStatistics(
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
    }

    private static MatchStatisticsResponse._PlayerInfoBasic createResponsePlayerInfoBasic(MatchStatsPlayers mpsDto) {
        return new MatchStatisticsResponse._PlayerInfoBasic(
                mpsDto.getId(),
                mpsDto.getName(),
                mpsDto.getKoreanName(),
                mpsDto.getPhotoUrl(),
                mpsDto.getPosition(),
                mpsDto.getNumber(),
                mpsDto.getTemporaryId() != null ? mpsDto.getTemporaryId().toString() : null
        );
    }

    private static int safeInt(Integer value) {
        return value != null ? value : 0;
    }
}
