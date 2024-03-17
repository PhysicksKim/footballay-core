package com.gyechunsik.scoreboard.domain.football.relations;

import com.gyechunsik.scoreboard.domain.football.league.League;
import com.gyechunsik.scoreboard.domain.football.team.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(LeagueTeamId.class)
public class LeagueTeam {

    @Id
    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;

    @Id
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

}
