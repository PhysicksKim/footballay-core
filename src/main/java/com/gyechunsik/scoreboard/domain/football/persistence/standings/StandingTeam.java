package com.gyechunsik.scoreboard.domain.football.persistence.standings;

import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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

}