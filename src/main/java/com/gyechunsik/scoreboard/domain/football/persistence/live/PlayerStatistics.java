package com.gyechunsik.scoreboard.domain.football.persistence.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
public class PlayerStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /**
     * 선수가 경기에서 뛴 총 시간(분 단위)
     * <br>예시: 97
     */
    private Integer minutesPlayed;

    /**
     * 선수의 포지션 (예: G: 골키퍼, D: 수비수, M: 미드필더, F: 공격수)
     * <br>예시: "G", "D", "M", "F"
     */
    private String position;

    /**
     * 선수의 경기 평점 (예: 7.2)
     * <br>예시: "7.2", "6.9"
     */
    private String rating;

    /**
     * 선수가 팀의 주장인지 여부 (true: 주장, false: 주장 아님)
     * <br>예시: true, false
     */
    private Boolean captain;

    /**
     * 선수가 교체 선수인지 여부 (true: 교체 선수, false: 선발 선수)
     * <br>예시: true, false
     */
    private Boolean substitute;

    /**
     * 선수의 총 슛 시도 횟수
     * <br>예시: 3
     */
    private Integer shotsTotal;

    /**
     * 선수가 골문으로 향한 슛 횟수
     * <br>예시: 1
     */
    private Integer shotsOn;

    /**
     * 선수가 득점한 총 골 수
     * <br>예시: 1
     */
    private Integer goals;

    /**
     * 선수가 실점한 총 골 수 (골키퍼에게 주로 사용됨)
     * <br>예시: 2
     */
    private Integer goalsConceded;

    /**
     * 선수가 기록한 어시스트 수
     * <br>예시: 1
     */
    private Integer assists;

    /**
     * 선수가 기록한 골키퍼 세이브 수 (골키퍼에게만 해당)
     * <br>예시: 2
     */
    private Integer saves;

    /**
     * 선수의 총 패스 수
     * <br>예시: 24, 43
     */
    private Integer passesTotal;

    /**
     * 선수의 키 패스 수 (득점 기회를 만들어낸 패스)
     * <br>예시: 2
     */
    private Integer passesKey;

    /**
     * 패스 정확도 (퍼센트로 기록됨, 예: "88%")
     * <br>예시: "27", "95"
     */
    private String passesAccuracy;

    /**
     * 선수가 기록한 총 태클 수
     * <br>예시: 3
     */
    private Integer tacklesTotal;

    /**
     * 선수가 기록한 인터셉트(상대 공을 차단한) 수
     * <br>예시: 1
     */
    private Integer interceptions;

    /**
     * 선수가 참여한 듀얼(경합) 횟수
     * <br>예시: 7
     */
    private Integer duelsTotal;

    /**
     * 선수가 이긴 듀얼 횟수
     * <br>예시: 5
     */
    private Integer duelsWon;

    /**
     * 선수의 드리블 시도 횟수
     * <br>예시: 3
     */
    private Integer dribblesAttempts;

    /**
     * 선수의 성공한 드리블 횟수
     * <br>예시: 2
     */
    private Integer dribblesSuccess;

    /**
     * 선수가 범한 파울 수
     * <br>예시: 2
     */
    private Integer foulsCommitted;

    /**
     * 선수가 당한 파울 수
     * <br>예시: 1
     */
    private Integer foulsDrawn;

    /**
     * 선수가 받은 옐로 카드 수
     * <br>예시: 1
     */
    private Integer yellowCards;

    /**
     * 선수가 받은 레드 카드 수
     * <br>예시: 0
     */
    private Integer redCards;

    /**
     * 선수가 성공시킨 페널티 킥 수
     * <br>예시: 0
     */
    private Integer penaltiesScored;

    /**
     * 선수가 놓친 페널티 킥 수
     * <br>예시: 0
     */
    private Integer penaltiesMissed;

    /**
     * 선수가 막아낸 페널티 킥 수 (골키퍼에게만 해당)
     * <br>예시: 0
     */
    private Integer penaltiesSaved;
}
