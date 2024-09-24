package com.gyechunsik.scoreboard.domain.football.entity.live;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.relational.core.sql.In;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
     * 볼 점유율 퍼센트 (Ball Possession) 이 값은 % 입니다
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
    @OneToMany(mappedBy = "teamStatistics", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<ExpectedGoals> expectedGoalsList;
}
