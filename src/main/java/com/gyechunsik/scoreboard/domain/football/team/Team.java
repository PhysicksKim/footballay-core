package com.gyechunsik.scoreboard.domain.football.team;

import com.gyechunsik.scoreboard.domain.football.league.League;
import com.gyechunsik.scoreboard.domain.football.relations.LeagueTeam;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

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

    public void updateCompare(Team other) {
        if(this.id != other.getId()) return;
        if(!Objects.equals(this.name, other.getName())) this.name = other.getName();
        if(!Objects.equals(this.logo, other.getLogo())) this.logo = other.getLogo();
    }
}
