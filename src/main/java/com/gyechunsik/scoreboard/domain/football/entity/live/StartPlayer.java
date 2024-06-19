package com.gyechunsik.scoreboard.domain.football.entity.live;

import com.gyechunsik.scoreboard.domain.football.entity.Player;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class StartPlayer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_lineup_id", nullable = false)
    private StartLineup startLineup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /**
     * G, D, M, F
     */
    private String position;

    /**
     * 경기장을 가로, 좌측 팀 기준으로 생각하고, x:y 형태로 표현합니다. <br>
     * y 는 Left 부터 시작입니다. (ex. 2:1 = 레프트백)
     * <pre>
     * 1  ------- X ------- x
     * |           2:1 (Left)
     * Y  GK 1:1   2:2
     * |           2:3
     * y           2:4
     * </pre>
     * 골키퍼는 1:1 수비수는 2:n 이후 3~x:n <br>
     * 교체선수인 경우 null 입니다.
     */
    @Column(nullable = true)
    private String grid;

    @Column(nullable = false)
    private Boolean substitute;

}
