package com.gyechunsik.scoreboard.domain.football.team;

import com.gyechunsik.scoreboard.domain.football.league.League;
import com.gyechunsik.scoreboard.domain.football.relations.LeagueTeam;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Team {

    @Id
    private long id;

    @Column(nullable = false)
    private String name;
    private String korean_name;
    private String logo;

    @ToString.Exclude
    @OneToMany(mappedBy = "team")
    private List<LeagueTeam> leagueTeams;
}
