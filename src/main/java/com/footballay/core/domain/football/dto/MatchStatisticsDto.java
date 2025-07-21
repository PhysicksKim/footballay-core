package com.footballay.core.domain.football.dto;

import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MatchStatisticsDto {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchStatisticsDto.class);
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


    public static class MatchStatsFixture {
        private long id;
        private String referee;
        private LocalDateTime date;
        private String timezone;
        private Long timestamp;
        private boolean available;
        private String round;

        public long getId() {
            return this.id;
        }

        public String getReferee() {
            return this.referee;
        }

        public LocalDateTime getDate() {
            return this.date;
        }

        public String getTimezone() {
            return this.timezone;
        }

        public Long getTimestamp() {
            return this.timestamp;
        }

        public boolean isAvailable() {
            return this.available;
        }

        public String getRound() {
            return this.round;
        }

        public MatchStatsFixture(final long id, final String referee, final LocalDateTime date, final String timezone, final Long timestamp, final boolean available, final String round) {
            this.id = id;
            this.referee = referee;
            this.date = date;
            this.timezone = timezone;
            this.timestamp = timestamp;
            this.available = available;
            this.round = round;
        }
    }


    public static class MatchStatsLiveStatus {
        private String longStatus;
        private String shortStatus;
        private Integer elapsed;
        private Integer homeScore;
        private Integer awayScore;

        public String getLongStatus() {
            return this.longStatus;
        }

        public String getShortStatus() {
            return this.shortStatus;
        }

        public Integer getElapsed() {
            return this.elapsed;
        }

        public Integer getHomeScore() {
            return this.homeScore;
        }

        public Integer getAwayScore() {
            return this.awayScore;
        }

        public MatchStatsLiveStatus(final String longStatus, final String shortStatus, final Integer elapsed, final Integer homeScore, final Integer awayScore) {
            this.longStatus = longStatus;
            this.shortStatus = shortStatus;
            this.elapsed = elapsed;
            this.homeScore = homeScore;
            this.awayScore = awayScore;
        }
    }


    public static class MatchStatsTeam {
        private Long id;
        private String name;
        private String koreanName;
        private String logo;

        public Long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getKoreanName() {
            return this.koreanName;
        }

        public String getLogo() {
            return this.logo;
        }

        public MatchStatsTeam(final Long id, final String name, final String koreanName, final String logo) {
            this.id = id;
            this.name = name;
            this.koreanName = koreanName;
            this.logo = logo;
        }
    }


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

        public Integer getShotsOnGoal() {
            return this.shotsOnGoal;
        }

        public Integer getShotsOffGoal() {
            return this.shotsOffGoal;
        }

        public Integer getTotalShots() {
            return this.totalShots;
        }

        public Integer getBlockedShots() {
            return this.blockedShots;
        }

        public Integer getShotsInsideBox() {
            return this.shotsInsideBox;
        }

        public Integer getShotsOutsideBox() {
            return this.shotsOutsideBox;
        }

        public Integer getFouls() {
            return this.fouls;
        }

        public Integer getCornerKicks() {
            return this.cornerKicks;
        }

        public Integer getOffsides() {
            return this.offsides;
        }

        public Integer getBallPossession() {
            return this.ballPossession;
        }

        public Integer getYellowCards() {
            return this.yellowCards;
        }

        public Integer getRedCards() {
            return this.redCards;
        }

        public Integer getGoalkeeperSaves() {
            return this.goalkeeperSaves;
        }

        public Integer getTotalPasses() {
            return this.totalPasses;
        }

        public Integer getPassesAccurate() {
            return this.passesAccurate;
        }

        public Integer getPassesAccuracyPercentage() {
            return this.passesAccuracyPercentage;
        }

        public Integer getGoalsPrevented() {
            return this.goalsPrevented;
        }

        public List<MatchStatsXg> getExpectedGoalsList() {
            return this.expectedGoalsList;
        }

        public MatchStatsTeamStatistics(final Integer shotsOnGoal, final Integer shotsOffGoal, final Integer totalShots, final Integer blockedShots, final Integer shotsInsideBox, final Integer shotsOutsideBox, final Integer fouls, final Integer cornerKicks, final Integer offsides, final Integer ballPossession, final Integer yellowCards, final Integer redCards, final Integer goalkeeperSaves, final Integer totalPasses, final Integer passesAccurate, final Integer passesAccuracyPercentage, final Integer goalsPrevented, final List<MatchStatsXg> expectedGoalsList) {
            this.shotsOnGoal = shotsOnGoal;
            this.shotsOffGoal = shotsOffGoal;
            this.totalShots = totalShots;
            this.blockedShots = blockedShots;
            this.shotsInsideBox = shotsInsideBox;
            this.shotsOutsideBox = shotsOutsideBox;
            this.fouls = fouls;
            this.cornerKicks = cornerKicks;
            this.offsides = offsides;
            this.ballPossession = ballPossession;
            this.yellowCards = yellowCards;
            this.redCards = redCards;
            this.goalkeeperSaves = goalkeeperSaves;
            this.totalPasses = totalPasses;
            this.passesAccurate = passesAccurate;
            this.passesAccuracyPercentage = passesAccuracyPercentage;
            this.goalsPrevented = goalsPrevented;
            this.expectedGoalsList = expectedGoalsList;
        }
    }


    public static class MatchStatsXg {
        private Integer elapsed;
        private String xg;

        public Integer getElapsed() {
            return this.elapsed;
        }

        public String getXg() {
            return this.xg;
        }

        public MatchStatsXg(final Integer elapsed, final String xg) {
            this.elapsed = elapsed;
            this.xg = xg;
        }
    }


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

        /**
         * Player 가 Unregistered Player 인 경우 id 가 null 일 수 있습니다.
         */
        @Nullable
        public Long getId() {
            return this.id;
        }

        /**
         * Player 가 Unregistered Player 인 경우 name 만 존재하거나 name 도 존재하지 않을 수 있습니다.
         */
        public String getName() {
            return this.name;
        }

        public String getKoreanName() {
            return this.koreanName;
        }

        public String getPhotoUrl() {
            return this.photoUrl;
        }

        public Integer getNumber() {
            return this.number;
        }

        public String getPosition() {
            return this.position;
        }

        public boolean isSubstitute() {
            return this.substitute;
        }

        public MatchStatsPlayerStatistics getStatistics() {
            return this.statistics;
        }

        public UUID getTemporaryId() {
            return this.temporaryId;
        }

        /**
         * Creates a new {@code MatchStatsPlayers} instance.
         *
         * @param id Player 가 Unregistered Player 인 경우 id 가 null 일 수 있습니다.
         * @param name Player 가 Unregistered Player 인 경우 name 만 존재하거나 name 도 존재하지 않을 수 있습니다.
         * @param koreanName
         * @param photoUrl
         * @param number
         * @param position
         * @param substitute
         * @param statistics
         * @param temporaryId
         */
        public MatchStatsPlayers(@Nullable final Long id, final String name, final String koreanName, final String photoUrl, final Integer number, final String position, final boolean substitute, final MatchStatsPlayerStatistics statistics, final UUID temporaryId) {
            this.id = id;
            this.name = name;
            this.koreanName = koreanName;
            this.photoUrl = photoUrl;
            this.number = number;
            this.position = position;
            this.substitute = substitute;
            this.statistics = statistics;
            this.temporaryId = temporaryId;
        }
    }


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

        public Integer getMinutesPlayed() {
            return this.minutesPlayed;
        }

        public String getPosition() {
            return this.position;
        }

        public String getRating() {
            return this.rating;
        }

        public Boolean getCaptain() {
            return this.captain;
        }

        public Boolean getSubstitute() {
            return this.substitute;
        }

        public Integer getShotsTotal() {
            return this.shotsTotal;
        }

        public Integer getShotsOn() {
            return this.shotsOn;
        }

        public Integer getGoals() {
            return this.goals;
        }

        public Integer getGoalsConceded() {
            return this.goalsConceded;
        }

        public Integer getAssists() {
            return this.assists;
        }

        public Integer getSaves() {
            return this.saves;
        }

        public Integer getPassesTotal() {
            return this.passesTotal;
        }

        public Integer getPassesKey() {
            return this.passesKey;
        }

        public Integer getPassesAccuracy() {
            return this.passesAccuracy;
        }

        public Integer getTacklesTotal() {
            return this.tacklesTotal;
        }

        public Integer getInterceptions() {
            return this.interceptions;
        }

        public Integer getDuelsTotal() {
            return this.duelsTotal;
        }

        public Integer getDuelsWon() {
            return this.duelsWon;
        }

        public Integer getDribblesAttempts() {
            return this.dribblesAttempts;
        }

        public Integer getDribblesSuccess() {
            return this.dribblesSuccess;
        }

        public Integer getFoulsCommitted() {
            return this.foulsCommitted;
        }

        public Integer getFoulsDrawn() {
            return this.foulsDrawn;
        }

        public Integer getYellowCards() {
            return this.yellowCards;
        }

        public Integer getRedCards() {
            return this.redCards;
        }

        public Integer getPenaltiesScored() {
            return this.penaltiesScored;
        }

        public Integer getPenaltiesMissed() {
            return this.penaltiesMissed;
        }

        public Integer getPenaltiesSaved() {
            return this.penaltiesSaved;
        }

        public MatchStatsPlayerStatistics(final Integer minutesPlayed, final String position, final String rating, final Boolean captain, final Boolean substitute, final Integer shotsTotal, final Integer shotsOn, final Integer goals, final Integer goalsConceded, final Integer assists, final Integer saves, final Integer passesTotal, final Integer passesKey, final Integer passesAccuracy, final Integer tacklesTotal, final Integer interceptions, final Integer duelsTotal, final Integer duelsWon, final Integer dribblesAttempts, final Integer dribblesSuccess, final Integer foulsCommitted, final Integer foulsDrawn, final Integer yellowCards, final Integer redCards, final Integer penaltiesScored, final Integer penaltiesMissed, final Integer penaltiesSaved) {
            this.minutesPlayed = minutesPlayed;
            this.position = position;
            this.rating = rating;
            this.captain = captain;
            this.substitute = substitute;
            this.shotsTotal = shotsTotal;
            this.shotsOn = shotsOn;
            this.goals = goals;
            this.goalsConceded = goalsConceded;
            this.assists = assists;
            this.saves = saves;
            this.passesTotal = passesTotal;
            this.passesKey = passesKey;
            this.passesAccuracy = passesAccuracy;
            this.tacklesTotal = tacklesTotal;
            this.interceptions = interceptions;
            this.duelsTotal = duelsTotal;
            this.duelsWon = duelsWon;
            this.dribblesAttempts = dribblesAttempts;
            this.dribblesSuccess = dribblesSuccess;
            this.foulsCommitted = foulsCommitted;
            this.foulsDrawn = foulsDrawn;
            this.yellowCards = yellowCards;
            this.redCards = redCards;
            this.penaltiesScored = penaltiesScored;
            this.penaltiesMissed = penaltiesMissed;
            this.penaltiesSaved = penaltiesSaved;
        }
    }

    @Override
    public String toString() {
        return "MatchStatisticsDto{" + "fixture.id=" + fixture.getId() + ", liveStatus.elapsed=" + liveStatus.getElapsed() + ", home.id=" + home.getId() + ", away.id=" + away.getId() + ", List<homePlayerStatistics>.size()=" + homePlayerStatistics.size() + ", List<awayPlayerStatistics>.size()=" + awayPlayerStatistics.size() + '}';
    }


    public static class MatchStatisticsDtoBuilder {
        private MatchStatsFixture fixture;
        private MatchStatsLiveStatus liveStatus;
        private MatchStatsTeam home;
        private MatchStatsTeam away;
        private MatchStatsTeamStatistics homeStatistics;
        private MatchStatsTeamStatistics awayStatistics;
        private List<MatchStatsPlayers> homePlayerStatistics;
        private List<MatchStatsPlayers> awayPlayerStatistics;

        MatchStatisticsDtoBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public MatchStatisticsDto.MatchStatisticsDtoBuilder fixture(final MatchStatsFixture fixture) {
            this.fixture = fixture;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchStatisticsDto.MatchStatisticsDtoBuilder liveStatus(final MatchStatsLiveStatus liveStatus) {
            this.liveStatus = liveStatus;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchStatisticsDto.MatchStatisticsDtoBuilder home(final MatchStatsTeam home) {
            this.home = home;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchStatisticsDto.MatchStatisticsDtoBuilder away(final MatchStatsTeam away) {
            this.away = away;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchStatisticsDto.MatchStatisticsDtoBuilder homeStatistics(@Nullable final MatchStatsTeamStatistics homeStatistics) {
            this.homeStatistics = homeStatistics;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchStatisticsDto.MatchStatisticsDtoBuilder awayStatistics(@Nullable final MatchStatsTeamStatistics awayStatistics) {
            this.awayStatistics = awayStatistics;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchStatisticsDto.MatchStatisticsDtoBuilder homePlayerStatistics(final List<MatchStatsPlayers> homePlayerStatistics) {
            this.homePlayerStatistics = homePlayerStatistics;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchStatisticsDto.MatchStatisticsDtoBuilder awayPlayerStatistics(final List<MatchStatsPlayers> awayPlayerStatistics) {
            this.awayPlayerStatistics = awayPlayerStatistics;
            return this;
        }

        public MatchStatisticsDto build() {
            return new MatchStatisticsDto(this.fixture, this.liveStatus, this.home, this.away, this.homeStatistics, this.awayStatistics, this.homePlayerStatistics, this.awayPlayerStatistics);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "MatchStatisticsDto.MatchStatisticsDtoBuilder(fixture=" + this.fixture + ", liveStatus=" + this.liveStatus + ", home=" + this.home + ", away=" + this.away + ", homeStatistics=" + this.homeStatistics + ", awayStatistics=" + this.awayStatistics + ", homePlayerStatistics=" + this.homePlayerStatistics + ", awayPlayerStatistics=" + this.awayPlayerStatistics + ")";
        }
    }

    public static MatchStatisticsDto.MatchStatisticsDtoBuilder builder() {
        return new MatchStatisticsDto.MatchStatisticsDtoBuilder();
    }

    public MatchStatsFixture getFixture() {
        return this.fixture;
    }

    public MatchStatsLiveStatus getLiveStatus() {
        return this.liveStatus;
    }

    public MatchStatsTeam getHome() {
        return this.home;
    }

    public MatchStatsTeam getAway() {
        return this.away;
    }

    @Nullable
    public MatchStatsTeamStatistics getHomeStatistics() {
        return this.homeStatistics;
    }

    @Nullable
    public MatchStatsTeamStatistics getAwayStatistics() {
        return this.awayStatistics;
    }

    public List<MatchStatsPlayers> getHomePlayerStatistics() {
        return this.homePlayerStatistics;
    }

    public List<MatchStatsPlayers> getAwayPlayerStatistics() {
        return this.awayPlayerStatistics;
    }

    public MatchStatisticsDto(final MatchStatsFixture fixture, final MatchStatsLiveStatus liveStatus, final MatchStatsTeam home, final MatchStatsTeam away, @Nullable final MatchStatsTeamStatistics homeStatistics, @Nullable final MatchStatsTeamStatistics awayStatistics, final List<MatchStatsPlayers> homePlayerStatistics, final List<MatchStatsPlayers> awayPlayerStatistics) {
        this.fixture = fixture;
        this.liveStatus = liveStatus;
        this.home = home;
        this.away = away;
        this.homeStatistics = homeStatistics;
        this.awayStatistics = awayStatistics;
        this.homePlayerStatistics = homePlayerStatistics;
        this.awayPlayerStatistics = awayPlayerStatistics;
    }
}
