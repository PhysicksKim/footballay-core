package com.footballay.core.domain.football.persistence.standings;

import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "standing_team")
public class StandingTeam extends BaseDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standing_id", nullable = false)
    private Standing standing;

    private int rank;

    private int points;

    @Column(name = "goals_diff")
    private int goalsDiff;

    @Column(name = "group_name")
    private String groupName;

    /**
     * Form 은 최근 5경기의 승무패를 보여주며, 시즌 초 5경기 미만인 경우 어떻게 표시될지 테스트가 필요합니다. <br>
     * ex. "WDWLD" (최근 W) <br>
     * W: Win, D: Draw, L: Lose <br>
     * index 작을수록 최신 경기 (index 0: 최신 경기)
     */
    private String form;

    private String description;

    private String status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "played", column = @Column(name = "all_played")),
            @AttributeOverride(name = "win", column = @Column(name = "all_win")),
            @AttributeOverride(name = "draw", column = @Column(name = "all_draw")),
            @AttributeOverride(name = "lose", column = @Column(name = "all_lose")),
            @AttributeOverride(name = "goalsFor", column = @Column(name = "all_goals_for")),
            @AttributeOverride(name = "goalsAgainst", column = @Column(name = "all_goals_against"))
    })
    private StandingStats allStats;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "played", column = @Column(name = "home_played")),
            @AttributeOverride(name = "win", column = @Column(name = "home_win")),
            @AttributeOverride(name = "draw", column = @Column(name = "home_draw")),
            @AttributeOverride(name = "lose", column = @Column(name = "home_lose")),
            @AttributeOverride(name = "goalsFor", column = @Column(name = "home_goals_for")),
            @AttributeOverride(name = "goalsAgainst", column = @Column(name = "home_goals_against"))
    })
    private StandingStats homeStats;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "played", column = @Column(name = "away_played")),
            @AttributeOverride(name = "win", column = @Column(name = "away_win")),
            @AttributeOverride(name = "draw", column = @Column(name = "away_draw")),
            @AttributeOverride(name = "lose", column = @Column(name = "away_lose")),
            @AttributeOverride(name = "goalsFor", column = @Column(name = "away_goals_for")),
            @AttributeOverride(name = "goalsAgainst", column = @Column(name = "away_goals_against"))
    })
    private StandingStats awayStats;

    // Protected no-args constructor for JPA
    protected StandingTeam() {
    }

    // Private all-args constructor for builder
    private StandingTeam(Long id, Team team, Standing standing, int rank, int points, int goalsDiff,
                         String groupName, String form, String description, String status,
                         StandingStats allStats, StandingStats homeStats, StandingStats awayStats) {
        this.id = id;
        this.team = team;
        this.standing = standing;
        this.rank = rank;
        this.points = points;
        this.goalsDiff = goalsDiff;
        this.groupName = groupName;
        this.form = form;
        this.description = description;
        this.status = status;
        this.allStats = allStats;
        this.homeStats = homeStats;
        this.awayStats = awayStats;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public Standing getStanding() {
        return standing;
    }

    public int getRank() {
        return rank;
    }

    public int getPoints() {
        return points;
    }

    public int getGoalsDiff() {
        return goalsDiff;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getForm() {
        return form;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public StandingStats getAllStats() {
        return allStats;
    }

    public StandingStats getHomeStats() {
        return homeStats;
    }

    public StandingStats getAwayStats() {
        return awayStats;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void setStanding(Standing standing) {
        this.standing = standing;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setGoalsDiff(int goalsDiff) {
        this.goalsDiff = goalsDiff;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAllStats(StandingStats allStats) {
        this.allStats = allStats;
    }

    public void setHomeStats(StandingStats homeStats) {
        this.homeStats = homeStats;
    }

    public void setAwayStats(StandingStats awayStats) {
        this.awayStats = awayStats;
    }

    // Builder
    public static StandingTeamBuilder builder() {
        return new StandingTeamBuilder();
    }

    public static class StandingTeamBuilder {
        private Long id;
        private Team team;
        private Standing standing;
        private int rank;
        private int points;
        private int goalsDiff;
        private String groupName;
        private String form;
        private String description;
        private String status;
        private StandingStats allStats;
        private StandingStats homeStats;
        private StandingStats awayStats;

        StandingTeamBuilder() {
        }

        public StandingTeamBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public StandingTeamBuilder team(Team team) {
            this.team = team;
            return this;
        }

        public StandingTeamBuilder standing(Standing standing) {
            this.standing = standing;
            return this;
        }

        public StandingTeamBuilder rank(int rank) {
            this.rank = rank;
            return this;
        }

        public StandingTeamBuilder points(int points) {
            this.points = points;
            return this;
        }

        public StandingTeamBuilder goalsDiff(int goalsDiff) {
            this.goalsDiff = goalsDiff;
            return this;
        }

        public StandingTeamBuilder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public StandingTeamBuilder form(String form) {
            this.form = form;
            return this;
        }

        public StandingTeamBuilder description(String description) {
            this.description = description;
            return this;
        }

        public StandingTeamBuilder status(String status) {
            this.status = status;
            return this;
        }

        public StandingTeamBuilder allStats(StandingStats allStats) {
            this.allStats = allStats;
            return this;
        }

        public StandingTeamBuilder homeStats(StandingStats homeStats) {
            this.homeStats = homeStats;
            return this;
        }

        public StandingTeamBuilder awayStats(StandingStats awayStats) {
            this.awayStats = awayStats;
            return this;
        }

        public StandingTeam build() {
            return new StandingTeam(id, team, standing, rank, points, goalsDiff, groupName,
                    form, description, status, allStats, homeStats, awayStats);
        }
    }
}