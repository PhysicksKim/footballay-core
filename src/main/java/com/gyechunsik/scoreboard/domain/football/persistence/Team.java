package com.gyechunsik.scoreboard.domain.football.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.TeamPlayer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
    private String koreanName;
    private String logo;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "team")
    private List<LeagueTeam> leagueTeams;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "team")
    private List<TeamPlayer> teamPlayers;

    public void updateCompare(Team other) {
        if(this.id != other.getId()) return;
        if(!Objects.equals(this.name, other.getName())) this.name = other.getName();
        if(!Objects.equals(this.logo, other.getLogo())) this.logo = other.getLogo();
    }
}
