package com.gyechunsik.scoreboard.domain.football.persistence.standings;

import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 리그의 시즌별 Standings 를 저장하는 Entity 입니다.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "standing")
public class Standing extends BaseDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    private int season;

    @Builder.Default
    @OneToMany(mappedBy = "standing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StandingTeam> standingTeams = new ArrayList<>();

    public void addStandingTeam(StandingTeam standingTeam) {
        standingTeams.add(standingTeam);
        standingTeam.setStanding(this);
    }


}