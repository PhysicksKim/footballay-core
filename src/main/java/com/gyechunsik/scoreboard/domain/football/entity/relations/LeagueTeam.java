package com.gyechunsik.scoreboard.domain.football.entity.relations;

import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class LeagueTeam {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Override
    public String toString() {
        return "LeagueTeam{" +
                "league=" + league +
                ", team=" + team +
                '}';
    }
}
