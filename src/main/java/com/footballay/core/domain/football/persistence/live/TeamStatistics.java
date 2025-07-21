package com.footballay.core.domain.football.persistence.live;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.Team;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class TeamStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    /**
     * 골대 안으로 향한 슛 수 (Shots on Goal)
     * <br>예시: 3
     */
    private Integer shotsOnGoal;
    /**
     * 골대 밖으로 나간 슛 수 (Shots off Goal)
     * <br>예시: 4
     */
    private Integer shotsOffGoal;
    /**
     * 총 슛 시도 수 (Total Shots)
     * <br>예시: 14
     */
    private Integer totalShots;
    /**
     * 블로킹된 슛 수 (Blocked Shots)
     * <br>예시: 7
     */
    private Integer blockedShots;
    /**
     * 박스 안에서의 슛 수 (Shots Inside Box)
     * <br>예시: 8
     */
    private Integer shotsInsideBox;
    /**
     * 박스 밖에서의 슛 수 (Shots Outside Box)
     * <br>예시: 6
     */
    private Integer shotsOutsideBox;
    /**
     * 파울 수 (Fouls)
     * <br>예시: 12
     */
    private Integer fouls;
    /**
     * 코너킥 수 (Corner Kicks)
     * <br>예시: 9
     */
    private Integer cornerKicks;
    /**
     * 오프사이드 횟수 (Offsides)
     * <br>예시: 0
     */
    private Integer offsides;
    /**
     * 볼 점유율 퍼센트 (Ball Possession) <br>
     * 이 값은 % 입니다
     * <br>예시: 71
     */
    private Integer ballPossession;
    /**
     * 옐로카드 수 (Yellow Cards)
     * <br>예시: 3
     */
    private Integer yellowCards;
    /**
     * 레드카드 수 (Red Cards)
     * <br>예시: 0
     */
    private Integer redCards;
    /**
     * 골키퍼 세이브 수 (Goalkeeper Saves)
     * <br>예시: 2
     */
    private Integer goalkeeperSaves;
    /**
     * 총 패스 수 (Total Passes)
     * <br>예시: 539
     */
    private Integer totalPasses;
    /**
     * 정확한 패스 수 (Passes Accurate)
     * <br>예시: 473
     */
    private Integer passesAccurate;
    /**
     * 패스 정확도 퍼센트 (Passes %) 이 값은 % 입니다
     * <br>예시: 88
     */
    private Integer passesAccuracyPercentage;
    /**
     * 방어한 골 수 (Goals Prevented)
     * <br>예시: 0
     */
    private Integer goalsPrevented;
    /**
     * 시간별 xG 값 리스트 (Expected Goals)
     * <br>예시: [0.1, 0.2, 0.3, 0.4, 0.5]
     */
    @OneToMany(mappedBy = "teamStatistics", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExpectedGoals> expectedGoalsList;

    @Override
    public String toString() {
        return "TeamStatistics{" + " shotsOnGoal=" + shotsOnGoal + ", shotsOffGoal=" + shotsOffGoal + ", totalShots=" + totalShots + ", blockedShots=" + blockedShots + ", fouls=" + fouls + ", cornerKicks=" + cornerKicks + ", offsides=" + offsides + ", ballPossession=" + ballPossession + ", yellowCards=" + yellowCards + ", redCards=" + redCards + ", goalkeeperSaves=" + goalkeeperSaves + ", totalPasses=" + totalPasses + ", passesAccurate=" + passesAccurate + ", passesAccuracyPercentage=" + passesAccuracyPercentage + ", goalsPrevented=" + goalsPrevented + '}';
    }

    public String getXgString() {
        return  // 시간순으로 정렬
        this.getExpectedGoalsList().stream().sorted(Comparator.comparing(ExpectedGoals::getElapsed)).map(goal -> String.format("{ time: %d, xg: %s }", goal.getElapsed(), goal.getXg())).collect(Collectors.joining(", ")); // 리스트의 각 요소를 쉼표로 연결
    }

    private static List<ExpectedGoals> $default$expectedGoalsList() {
        return new ArrayList<>();
    }


    public static class TeamStatisticsBuilder {
        private Long id;
        private Fixture fixture;
        private Team team;
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
        private boolean expectedGoalsList$set;
        private List<ExpectedGoals> expectedGoalsList$value;

        TeamStatisticsBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder fixture(final Fixture fixture) {
            this.fixture = fixture;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder team(final Team team) {
            this.team = team;
            return this;
        }

        /**
         * 골대 안으로 향한 슛 수 (Shots on Goal)
         * <br>예시: 3
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder shotsOnGoal(final Integer shotsOnGoal) {
            this.shotsOnGoal = shotsOnGoal;
            return this;
        }

        /**
         * 골대 밖으로 나간 슛 수 (Shots off Goal)
         * <br>예시: 4
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder shotsOffGoal(final Integer shotsOffGoal) {
            this.shotsOffGoal = shotsOffGoal;
            return this;
        }

        /**
         * 총 슛 시도 수 (Total Shots)
         * <br>예시: 14
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder totalShots(final Integer totalShots) {
            this.totalShots = totalShots;
            return this;
        }

        /**
         * 블로킹된 슛 수 (Blocked Shots)
         * <br>예시: 7
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder blockedShots(final Integer blockedShots) {
            this.blockedShots = blockedShots;
            return this;
        }

        /**
         * 박스 안에서의 슛 수 (Shots Inside Box)
         * <br>예시: 8
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder shotsInsideBox(final Integer shotsInsideBox) {
            this.shotsInsideBox = shotsInsideBox;
            return this;
        }

        /**
         * 박스 밖에서의 슛 수 (Shots Outside Box)
         * <br>예시: 6
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder shotsOutsideBox(final Integer shotsOutsideBox) {
            this.shotsOutsideBox = shotsOutsideBox;
            return this;
        }

        /**
         * 파울 수 (Fouls)
         * <br>예시: 12
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder fouls(final Integer fouls) {
            this.fouls = fouls;
            return this;
        }

        /**
         * 코너킥 수 (Corner Kicks)
         * <br>예시: 9
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder cornerKicks(final Integer cornerKicks) {
            this.cornerKicks = cornerKicks;
            return this;
        }

        /**
         * 오프사이드 횟수 (Offsides)
         * <br>예시: 0
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder offsides(final Integer offsides) {
            this.offsides = offsides;
            return this;
        }

        /**
         * 볼 점유율 퍼센트 (Ball Possession) <br>
         * 이 값은 % 입니다
         * <br>예시: 71
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder ballPossession(final Integer ballPossession) {
            this.ballPossession = ballPossession;
            return this;
        }

        /**
         * 옐로카드 수 (Yellow Cards)
         * <br>예시: 3
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder yellowCards(final Integer yellowCards) {
            this.yellowCards = yellowCards;
            return this;
        }

        /**
         * 레드카드 수 (Red Cards)
         * <br>예시: 0
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder redCards(final Integer redCards) {
            this.redCards = redCards;
            return this;
        }

        /**
         * 골키퍼 세이브 수 (Goalkeeper Saves)
         * <br>예시: 2
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder goalkeeperSaves(final Integer goalkeeperSaves) {
            this.goalkeeperSaves = goalkeeperSaves;
            return this;
        }

        /**
         * 총 패스 수 (Total Passes)
         * <br>예시: 539
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder totalPasses(final Integer totalPasses) {
            this.totalPasses = totalPasses;
            return this;
        }

        /**
         * 정확한 패스 수 (Passes Accurate)
         * <br>예시: 473
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder passesAccurate(final Integer passesAccurate) {
            this.passesAccurate = passesAccurate;
            return this;
        }

        /**
         * 패스 정확도 퍼센트 (Passes %) 이 값은 % 입니다
         * <br>예시: 88
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder passesAccuracyPercentage(final Integer passesAccuracyPercentage) {
            this.passesAccuracyPercentage = passesAccuracyPercentage;
            return this;
        }

        /**
         * 방어한 골 수 (Goals Prevented)
         * <br>예시: 0
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder goalsPrevented(final Integer goalsPrevented) {
            this.goalsPrevented = goalsPrevented;
            return this;
        }

        /**
         * 시간별 xG 값 리스트 (Expected Goals)
         * <br>예시: [0.1, 0.2, 0.3, 0.4, 0.5]
         * @return {@code this}.
         */
        public TeamStatistics.TeamStatisticsBuilder expectedGoalsList(final List<ExpectedGoals> expectedGoalsList) {
            this.expectedGoalsList$value = expectedGoalsList;
            expectedGoalsList$set = true;
            return this;
        }

        public TeamStatistics build() {
            List<ExpectedGoals> expectedGoalsList$value = this.expectedGoalsList$value;
            if (!this.expectedGoalsList$set) expectedGoalsList$value = TeamStatistics.$default$expectedGoalsList();
            return new TeamStatistics(this.id, this.fixture, this.team, this.shotsOnGoal, this.shotsOffGoal, this.totalShots, this.blockedShots, this.shotsInsideBox, this.shotsOutsideBox, this.fouls, this.cornerKicks, this.offsides, this.ballPossession, this.yellowCards, this.redCards, this.goalkeeperSaves, this.totalPasses, this.passesAccurate, this.passesAccuracyPercentage, this.goalsPrevented, expectedGoalsList$value);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "TeamStatistics.TeamStatisticsBuilder(id=" + this.id + ", fixture=" + this.fixture + ", team=" + this.team + ", shotsOnGoal=" + this.shotsOnGoal + ", shotsOffGoal=" + this.shotsOffGoal + ", totalShots=" + this.totalShots + ", blockedShots=" + this.blockedShots + ", shotsInsideBox=" + this.shotsInsideBox + ", shotsOutsideBox=" + this.shotsOutsideBox + ", fouls=" + this.fouls + ", cornerKicks=" + this.cornerKicks + ", offsides=" + this.offsides + ", ballPossession=" + this.ballPossession + ", yellowCards=" + this.yellowCards + ", redCards=" + this.redCards + ", goalkeeperSaves=" + this.goalkeeperSaves + ", totalPasses=" + this.totalPasses + ", passesAccurate=" + this.passesAccurate + ", passesAccuracyPercentage=" + this.passesAccuracyPercentage + ", goalsPrevented=" + this.goalsPrevented + ", expectedGoalsList$value=" + this.expectedGoalsList$value + ")";
        }
    }

    public static TeamStatistics.TeamStatisticsBuilder builder() {
        return new TeamStatistics.TeamStatisticsBuilder();
    }

    public TeamStatistics() {
        this.expectedGoalsList = TeamStatistics.$default$expectedGoalsList();
    }

    /**
     * Creates a new {@code TeamStatistics} instance.
     *
     * @param id
     * @param fixture
     * @param team
     * @param shotsOnGoal 골대 안으로 향한 슛 수 (Shots on Goal)
     * <br>예시: 3
     * @param shotsOffGoal 골대 밖으로 나간 슛 수 (Shots off Goal)
     * <br>예시: 4
     * @param totalShots 총 슛 시도 수 (Total Shots)
     * <br>예시: 14
     * @param blockedShots 블로킹된 슛 수 (Blocked Shots)
     * <br>예시: 7
     * @param shotsInsideBox 박스 안에서의 슛 수 (Shots Inside Box)
     * <br>예시: 8
     * @param shotsOutsideBox 박스 밖에서의 슛 수 (Shots Outside Box)
     * <br>예시: 6
     * @param fouls 파울 수 (Fouls)
     * <br>예시: 12
     * @param cornerKicks 코너킥 수 (Corner Kicks)
     * <br>예시: 9
     * @param offsides 오프사이드 횟수 (Offsides)
     * <br>예시: 0
     * @param ballPossession 볼 점유율 퍼센트 (Ball Possession) <br>
     * 이 값은 % 입니다
     * <br>예시: 71
     * @param yellowCards 옐로카드 수 (Yellow Cards)
     * <br>예시: 3
     * @param redCards 레드카드 수 (Red Cards)
     * <br>예시: 0
     * @param goalkeeperSaves 골키퍼 세이브 수 (Goalkeeper Saves)
     * <br>예시: 2
     * @param totalPasses 총 패스 수 (Total Passes)
     * <br>예시: 539
     * @param passesAccurate 정확한 패스 수 (Passes Accurate)
     * <br>예시: 473
     * @param passesAccuracyPercentage 패스 정확도 퍼센트 (Passes %) 이 값은 % 입니다
     * <br>예시: 88
     * @param goalsPrevented 방어한 골 수 (Goals Prevented)
     * <br>예시: 0
     * @param expectedGoalsList 시간별 xG 값 리스트 (Expected Goals)
     * <br>예시: [0.1, 0.2, 0.3, 0.4, 0.5]
     */
    public TeamStatistics(final Long id, final Fixture fixture, final Team team, final Integer shotsOnGoal, final Integer shotsOffGoal, final Integer totalShots, final Integer blockedShots, final Integer shotsInsideBox, final Integer shotsOutsideBox, final Integer fouls, final Integer cornerKicks, final Integer offsides, final Integer ballPossession, final Integer yellowCards, final Integer redCards, final Integer goalkeeperSaves, final Integer totalPasses, final Integer passesAccurate, final Integer passesAccuracyPercentage, final Integer goalsPrevented, final List<ExpectedGoals> expectedGoalsList) {
        this.id = id;
        this.fixture = fixture;
        this.team = team;
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

    public Long getId() {
        return this.id;
    }

    public Fixture getFixture() {
        return this.fixture;
    }

    public Team getTeam() {
        return this.team;
    }

    /**
     * 골대 안으로 향한 슛 수 (Shots on Goal)
     * <br>예시: 3
     */
    public Integer getShotsOnGoal() {
        return this.shotsOnGoal;
    }

    /**
     * 골대 밖으로 나간 슛 수 (Shots off Goal)
     * <br>예시: 4
     */
    public Integer getShotsOffGoal() {
        return this.shotsOffGoal;
    }

    /**
     * 총 슛 시도 수 (Total Shots)
     * <br>예시: 14
     */
    public Integer getTotalShots() {
        return this.totalShots;
    }

    /**
     * 블로킹된 슛 수 (Blocked Shots)
     * <br>예시: 7
     */
    public Integer getBlockedShots() {
        return this.blockedShots;
    }

    /**
     * 박스 안에서의 슛 수 (Shots Inside Box)
     * <br>예시: 8
     */
    public Integer getShotsInsideBox() {
        return this.shotsInsideBox;
    }

    /**
     * 박스 밖에서의 슛 수 (Shots Outside Box)
     * <br>예시: 6
     */
    public Integer getShotsOutsideBox() {
        return this.shotsOutsideBox;
    }

    /**
     * 파울 수 (Fouls)
     * <br>예시: 12
     */
    public Integer getFouls() {
        return this.fouls;
    }

    /**
     * 코너킥 수 (Corner Kicks)
     * <br>예시: 9
     */
    public Integer getCornerKicks() {
        return this.cornerKicks;
    }

    /**
     * 오프사이드 횟수 (Offsides)
     * <br>예시: 0
     */
    public Integer getOffsides() {
        return this.offsides;
    }

    /**
     * 볼 점유율 퍼센트 (Ball Possession) <br>
     * 이 값은 % 입니다
     * <br>예시: 71
     */
    public Integer getBallPossession() {
        return this.ballPossession;
    }

    /**
     * 옐로카드 수 (Yellow Cards)
     * <br>예시: 3
     */
    public Integer getYellowCards() {
        return this.yellowCards;
    }

    /**
     * 레드카드 수 (Red Cards)
     * <br>예시: 0
     */
    public Integer getRedCards() {
        return this.redCards;
    }

    /**
     * 골키퍼 세이브 수 (Goalkeeper Saves)
     * <br>예시: 2
     */
    public Integer getGoalkeeperSaves() {
        return this.goalkeeperSaves;
    }

    /**
     * 총 패스 수 (Total Passes)
     * <br>예시: 539
     */
    public Integer getTotalPasses() {
        return this.totalPasses;
    }

    /**
     * 정확한 패스 수 (Passes Accurate)
     * <br>예시: 473
     */
    public Integer getPassesAccurate() {
        return this.passesAccurate;
    }

    /**
     * 패스 정확도 퍼센트 (Passes %) 이 값은 % 입니다
     * <br>예시: 88
     */
    public Integer getPassesAccuracyPercentage() {
        return this.passesAccuracyPercentage;
    }

    /**
     * 방어한 골 수 (Goals Prevented)
     * <br>예시: 0
     */
    public Integer getGoalsPrevented() {
        return this.goalsPrevented;
    }

    /**
     * 시간별 xG 값 리스트 (Expected Goals)
     * <br>예시: [0.1, 0.2, 0.3, 0.4, 0.5]
     */
    public List<ExpectedGoals> getExpectedGoalsList() {
        return this.expectedGoalsList;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setFixture(final Fixture fixture) {
        this.fixture = fixture;
    }

    public void setTeam(final Team team) {
        this.team = team;
    }

    /**
     * 골대 안으로 향한 슛 수 (Shots on Goal)
     * <br>예시: 3
     */
    public void setShotsOnGoal(final Integer shotsOnGoal) {
        this.shotsOnGoal = shotsOnGoal;
    }

    /**
     * 골대 밖으로 나간 슛 수 (Shots off Goal)
     * <br>예시: 4
     */
    public void setShotsOffGoal(final Integer shotsOffGoal) {
        this.shotsOffGoal = shotsOffGoal;
    }

    /**
     * 총 슛 시도 수 (Total Shots)
     * <br>예시: 14
     */
    public void setTotalShots(final Integer totalShots) {
        this.totalShots = totalShots;
    }

    /**
     * 블로킹된 슛 수 (Blocked Shots)
     * <br>예시: 7
     */
    public void setBlockedShots(final Integer blockedShots) {
        this.blockedShots = blockedShots;
    }

    /**
     * 박스 안에서의 슛 수 (Shots Inside Box)
     * <br>예시: 8
     */
    public void setShotsInsideBox(final Integer shotsInsideBox) {
        this.shotsInsideBox = shotsInsideBox;
    }

    /**
     * 박스 밖에서의 슛 수 (Shots Outside Box)
     * <br>예시: 6
     */
    public void setShotsOutsideBox(final Integer shotsOutsideBox) {
        this.shotsOutsideBox = shotsOutsideBox;
    }

    /**
     * 파울 수 (Fouls)
     * <br>예시: 12
     */
    public void setFouls(final Integer fouls) {
        this.fouls = fouls;
    }

    /**
     * 코너킥 수 (Corner Kicks)
     * <br>예시: 9
     */
    public void setCornerKicks(final Integer cornerKicks) {
        this.cornerKicks = cornerKicks;
    }

    /**
     * 오프사이드 횟수 (Offsides)
     * <br>예시: 0
     */
    public void setOffsides(final Integer offsides) {
        this.offsides = offsides;
    }

    /**
     * 볼 점유율 퍼센트 (Ball Possession) <br>
     * 이 값은 % 입니다
     * <br>예시: 71
     */
    public void setBallPossession(final Integer ballPossession) {
        this.ballPossession = ballPossession;
    }

    /**
     * 옐로카드 수 (Yellow Cards)
     * <br>예시: 3
     */
    public void setYellowCards(final Integer yellowCards) {
        this.yellowCards = yellowCards;
    }

    /**
     * 레드카드 수 (Red Cards)
     * <br>예시: 0
     */
    public void setRedCards(final Integer redCards) {
        this.redCards = redCards;
    }

    /**
     * 골키퍼 세이브 수 (Goalkeeper Saves)
     * <br>예시: 2
     */
    public void setGoalkeeperSaves(final Integer goalkeeperSaves) {
        this.goalkeeperSaves = goalkeeperSaves;
    }

    /**
     * 총 패스 수 (Total Passes)
     * <br>예시: 539
     */
    public void setTotalPasses(final Integer totalPasses) {
        this.totalPasses = totalPasses;
    }

    /**
     * 정확한 패스 수 (Passes Accurate)
     * <br>예시: 473
     */
    public void setPassesAccurate(final Integer passesAccurate) {
        this.passesAccurate = passesAccurate;
    }

    /**
     * 패스 정확도 퍼센트 (Passes %) 이 값은 % 입니다
     * <br>예시: 88
     */
    public void setPassesAccuracyPercentage(final Integer passesAccuracyPercentage) {
        this.passesAccuracyPercentage = passesAccuracyPercentage;
    }

    /**
     * 방어한 골 수 (Goals Prevented)
     * <br>예시: 0
     */
    public void setGoalsPrevented(final Integer goalsPrevented) {
        this.goalsPrevented = goalsPrevented;
    }

    /**
     * 시간별 xG 값 리스트 (Expected Goals)
     * <br>예시: [0.1, 0.2, 0.3, 0.4, 0.5]
     */
    public void setExpectedGoalsList(final List<ExpectedGoals> expectedGoalsList) {
        this.expectedGoalsList = expectedGoalsList;
    }
}
