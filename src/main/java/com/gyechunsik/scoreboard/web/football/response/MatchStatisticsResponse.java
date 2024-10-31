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

    public record _PlayerInfoBasic(
            Long id,
            String name,
            String koreanName,
            String photo,
            String position,
            Integer number
    ){}

    // TODO : player 상세 정보 추가 캐싱 필요
    /*
    player: {
        id: 1100
        name: "E. Haaland"
        firstname: "Erling"
        lastname: "Braut Haaland"
        age: 24
        birth: {
            date: "2000-07-21"
            place: "Leeds"
            country: "England"
        }
        nationality: "Norway"
        height: "194 cm"
        weight: "88 kg"
        injured: false
        photo: "https://media.api-sports.io/football/players/1100.png"
    }
     */
    public record _PlayerInfoDetail(){}

    public record _PlayerStatistics(
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

    public record _ResponsePlayerStatistics(
            _PlayerInfoBasic player,
            _PlayerStatistics statistics
    ) {
    }

    public record _ResponseTeamWithStatistics(
            _ResponseTeam team,
            _ResponseTeamStatistics teamStatistics,
            List<_ResponsePlayerStatistics> playerStatistics
    ) {
    }
}
