package com.gyechunsik.scoreboard.domain.football.dto;

import jakarta.annotation.Nullable;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
public class MatchStatisticsDto {

    private MatchStatsFixture fixture;
    private MatchStatsLiveStatus liveStatus;

    private MatchStatsTeam home;
    private MatchStatsTeam away;

    @Nullable
    private MatchStatsTeamStatistics homeStatistics;
    @Nullable
    private MatchStatsTeamStatistics awayStatistics;

    private List<MatchStatsPlayers> homePlayerStatistics;
    private List<MatchStatsPlayers> awayPlayerStatistics;

    @Getter
    @AllArgsConstructor
    public static class MatchStatsFixture {
        private long id;
        private String referee;
        private LocalDateTime date;
        private String timezone;
        private Long timestamp;
        private boolean available;
        private String round;
    }

    @Getter
    @AllArgsConstructor
    public static class MatchStatsLiveStatus {
        private String longStatus;
        private String shortStatus;
        private Integer elapsed;
        private Integer homeScore;
        private Integer awayScore;
    }

    @Getter
    @AllArgsConstructor
    public static class MatchStatsTeam {
        private Long id;
        private String name;
        private String koreanName;
        private String logo;
    }

    @Getter
    @AllArgsConstructor
    public static class MatchStatsTeamStatistics {
        private Integer shotsOnGoal;
        private Integer shotsOffGoal;
        private Integer totalShots;
        private Integer blockedShots;
        private Integer shotsInsideBox;
        private Integer shotsOutsideBox;
        private Integer fouls;
        private Integer cornerKicks;
        private Integer offsides;
        private Integer ballPossession;
        private Integer yellowCards;
        private Integer redCards;
        private Integer goalkeeperSaves;
        private Integer totalPasses;
        private Integer passesAccurate;
        private Integer passesAccuracyPercentage;
        private Integer goalsPrevented;
        private List<MatchStatsXg> expectedGoalsList;
    }

    @Getter
    @AllArgsConstructor
    public static class MatchStatsXg {
        private Integer elapsed;
        private String xg;
    }

    @Getter
    @AllArgsConstructor
    public static class MatchStatsPlayers {

        /**
         * Player 가 Unregistered Player 인 경우 id 가 null 일 수 있습니다.
         */
        @Nullable
        private Long id;

        /**
         * Player 가 Unregistered Player 인 경우 name 만 존재하거나 name 도 존재하지 않을 수 있습니다.
         */
        private String name;
        private String koreanName;
        private String photoUrl;
        private Integer number;

        private String position;
        private boolean substitute;
        private MatchStatsPlayerStatistics statistics;
        private UUID temporaryId;

    }

    @Getter
    @AllArgsConstructor
    public static class MatchStatsPlayerStatistics {
        private Integer minutesPlayed;
        private String position;
        private String rating;
        private Boolean captain;
        private Boolean substitute;
        private Integer shotsTotal;
        private Integer shotsOn;
        private Integer goals;
        private Integer goalsConceded;
        private Integer assists;
        private Integer saves;
        private Integer passesTotal;
        private Integer passesKey;
        private Integer passesAccuracy;
        private Integer tacklesTotal;
        private Integer interceptions;
        private Integer duelsTotal;
        private Integer duelsWon;
        private Integer dribblesAttempts;
        private Integer dribblesSuccess;
        private Integer foulsCommitted;
        private Integer foulsDrawn;
        private Integer yellowCards;
        private Integer redCards;
        private Integer penaltiesScored;
        private Integer penaltiesMissed;
        private Integer penaltiesSaved;
    }

    @Override
    public String toString() {
        return "MatchStatisticsDto{" +
                "fixture.id=" + fixture.getId() +
                ", liveStatus.elapsed=" + liveStatus.getElapsed() +
                ", home.id=" + home.getId() +
                ", away.id=" + away.getId() +
                ", List<homePlayerStatistics>.size()=" + homePlayerStatistics.size() +
                ", List<awayPlayerStatistics>.size()=" + awayPlayerStatistics.size() +
                '}';
    }
}
