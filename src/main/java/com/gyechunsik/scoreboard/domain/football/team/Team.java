package com.gyechunsik.scoreboard.domain.football.team;

import com.gyechunsik.scoreboard.domain.football.league.League;
import com.gyechunsik.scoreboard.domain.football.relations.LeagueTeam;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Team {

    @Id
    private long id;

    @Column(nullable = false)
    private String name;
    private String korean_name;
    private String logo;

    @OneToMany(mappedBy = "team")
    private List<LeagueTeam> leagueTeams;
}
