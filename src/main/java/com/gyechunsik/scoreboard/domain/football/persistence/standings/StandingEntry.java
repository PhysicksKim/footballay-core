package com.gyechunsik.scoreboard.domain.football.persistence.standings;

import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "standing_entries")
public class StandingEntry extends BaseDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rank;

    private int points;

    @Column(name = "goals_diff")
    private int goalsDiff;

    @Column(name = "group_name")
    private String groupName;

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
    private MatchStats allStats;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "played", column = @Column(name = "home_played")),
            @AttributeOverride(name = "win", column = @Column(name = "home_win")),
            @AttributeOverride(name = "draw", column = @Column(name = "home_draw")),
            @AttributeOverride(name = "lose", column = @Column(name = "home_lose")),
            @AttributeOverride(name = "goalsFor", column = @Column(name = "home_goals_for")),
            @AttributeOverride(name = "goalsAgainst", column = @Column(name = "home_goals_against"))
    })
    private MatchStats homeStats;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "played", column = @Column(name = "away_played")),
            @AttributeOverride(name = "win", column = @Column(name = "away_win")),
            @AttributeOverride(name = "draw", column = @Column(name = "away_draw")),
            @AttributeOverride(name = "lose", column = @Column(name = "away_lose")),
            @AttributeOverride(name = "goalsFor", column = @Column(name = "away_goals_for")),
            @AttributeOverride(name = "goalsAgainst", column = @Column(name = "away_goals_against"))
    })
    private MatchStats awayStats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standings_id", nullable = false)
    private Standings standings;

}