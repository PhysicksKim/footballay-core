package com.gyechunsik.scoreboard.domain.football.persistence.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import jakarta.persistence.*;
import lombok.*;

/**
 * Fixture Caching 과정과 Fixture Live Job 과정 둘에서 저장됩니다. <br>
 * {@link LiveStatus} 는 Live Job 데이터로 분류하지 않습니다. 즉, 비라이브 데이터라고 칭합니다. <br>
 * 라이브 데이터에 대한 정의는 {@link Fixture} 를 참조하십시오. <br>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class LiveStatus {

    @Id @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "liveStatus", fetch = FetchType.LAZY)
    private Fixture fixture;

    private String longStatus;
    private String shortStatus;
    private Integer elapsed;

    private Integer homeScore = 0;
    private Integer awayScore = 0;

    @Override
    public String toString() {
        return "LiveStatus{" +
                ", id=" + id +
                ", longStatus='" + longStatus + '\'' +
                ", shortStatus='" + shortStatus + '\'' +
                ", elapsed=" + elapsed +
                ", homeScore=" + homeScore +
                ", awayScore=" + awayScore +
                '}';
    }

    public void updateCompare(LiveStatus other) {
        this.longStatus = other.getLongStatus();
        this.shortStatus = other.getShortStatus();
        this.elapsed = other.getElapsed();

        try {
            this.homeScore = other.getHomeScore();
        } catch (NullPointerException e) {
            this.homeScore = 0;
        }
        try {
            this.awayScore = other.getAwayScore();
        } catch (NullPointerException e) {
            this.awayScore = 0;
        }
    }

}
