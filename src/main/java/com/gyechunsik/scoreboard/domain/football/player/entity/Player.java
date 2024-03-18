package com.gyechunsik.scoreboard.domain.football.player.entity;

import com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse;
import com.gyechunsik.scoreboard.domain.football.team.Team;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "players")
public class Player {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String koreanName;
    private String photoUrl;
    private String position;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    public Player(PlayerSquadResponse.PlayerData playerData) {
        this.id = playerData.getId();
        this.name = playerData.getName();
        this.photoUrl = playerData.getPhoto();
        this.position = playerData.getPosition();
    }

    public void updateFromApiData(PlayerSquadResponse.PlayerData apiData) {
        this.name = apiData.getName();
        this.photoUrl = apiData.getPhoto();
        this.position = apiData.getPosition();
    }
}