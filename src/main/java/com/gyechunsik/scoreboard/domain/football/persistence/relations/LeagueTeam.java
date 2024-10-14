package com.gyechunsik.scoreboard.domain.football.persistence.relations;

import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"league_id", "team_id"}))
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
