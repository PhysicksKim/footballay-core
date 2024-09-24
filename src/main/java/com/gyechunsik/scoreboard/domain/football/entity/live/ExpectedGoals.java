package com.gyechunsik.scoreboard.domain.football.entity.live;

import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class ExpectedGoals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_statistics_id", nullable = false)
    private TeamStatistics teamStatistics;

    /**
     * xG 값이 기록된 시간
     */
    private Integer elapsed;

    /**
     * xG 값 <br>
     * xG(expected goal) 값은 <code>String</code> 으로 처리합니다.
     * 높은 소수점 정확도가 필요없으며 소수점 이하 2자리 까지만 표기하는 게 통상적이고
     * 대부분 읽기에 사용되기 때문에 <code>String</code> 이 적합합니다.
     */
    private String xg;

}
