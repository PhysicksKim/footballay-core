package com.gyechunsik.scoreboard.domain.football.league;

import com.gyechunsik.scoreboard.domain.football.relations.LeagueTeam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "leagues")
public class League {

    @Id
    private Long leagueId;

    @Column(nullable = false)
    private String name;

    private String korean_name;
    private String logo;

    @ToString.Exclude
    @OneToMany(mappedBy = "league")
    private List<LeagueTeam> leagueTeams;
}
