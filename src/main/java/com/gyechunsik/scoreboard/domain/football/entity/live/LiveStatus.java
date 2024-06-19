package com.gyechunsik.scoreboard.domain.football.entity.live;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
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

}
