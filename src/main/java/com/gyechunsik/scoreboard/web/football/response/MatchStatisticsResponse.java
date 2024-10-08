package com.gyechunsik.scoreboard.web.football.response;

import java.util.List;

public record MatchStatisticsResponse(
        _ResponseFixture fixture,
        _ResponseTeamWithStatistics home,
        _ResponseTeamWithStatistics away
) {
    public record _ResponseFixture(
            long id,
            int elapsed,
            String status
    ) {
    }

    public record _ResponseTeam(
            long id,
            String name,
            String koreanName,
            String logo
    ) {
    }

    public record _XG(
            int elapsed,
            String xg
    ) {
    }

    public record _ResponseTeamStatistics(
            int shotsOnGoal,
            int shotsOffGoal,
            int totalShots,
            int blockedShots,
            int shotsInsideBox,
            int shotsOutsideBox,
            int fouls,
            int cornerKicks,
            int offsides,
            int ballPossession,
            int yellowCards,
            int redCards,
            int goalkeeperSaves,
            int totalPasses,
            int passesAccurate,
            int passesAccuracyPercentage,
            int goalsPrevented,
            List<_XG> xg
    ) {
    }

    // TODO : PlayerStatistics 안에 한방에 넣지 말고 나누자. { 선수기본정보, 선수상세정보(키,국적 등), 선수통계 }
    public record _ResponsePlayerStatistics(
            long id,
            String name,
            String koreanName,
            int minutesPlayed,
            String position,
            String rating,
            boolean captain,
            boolean substitute,
            int shotsTotal,
            int shotsOn,
            int goals,
            int goalsConceded,
            int assists,
            int saves,
            int passesTotal,
            int passesKey,
            int passesAccuracy,
            int tacklesTotal,
            int interceptions,
            int duelsTotal,
            int duelsWon,
            int dribblesAttempts,
            int dribblesSuccess,
            int foulsCommitted,
            int foulsDrawn,
            int yellowCards,
            int redCards,
            int penaltiesScored,
            int penaltiesMissed,
            int penaltiesSaved
    ) {
    }

    public record _ResponseTeamWithStatistics(
            _ResponseTeam team,
            _ResponseTeamStatistics teamStatistics,
            List<_ResponsePlayerStatistics> playerStatistics
    ) {
    }
}
