package com.gyechunsik.scoreboard.domain.football.entity.relations;

import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import jakarta.persistence.*;
import lombok.*;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(LeagueTeamId.class)
public class LeagueTeam {

    @Id
    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league; // 복합 키의 일부로 사용될 필드

    @Id
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team; // 복합 키의 일부로 사용될 필드

    @Override
    public String toString() {
        return "LeagueTeam{" +
                "league=" + league +
                ", team=" + team +
                '}';
    }
}
