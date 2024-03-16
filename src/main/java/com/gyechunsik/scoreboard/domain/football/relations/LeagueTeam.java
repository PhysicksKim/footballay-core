package com.gyechunsik.scoreboard.domain.football.relations;

import com.gyechunsik.scoreboard.domain.football.league.League;
import com.gyechunsik.scoreboard.domain.football.team.Team;
import jakarta.persistence.*;

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
