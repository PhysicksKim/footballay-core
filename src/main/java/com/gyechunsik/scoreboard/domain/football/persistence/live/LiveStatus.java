package com.gyechunsik.scoreboard.domain.football.persistence.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import jakarta.persistence.*;
import lombok.*;

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
