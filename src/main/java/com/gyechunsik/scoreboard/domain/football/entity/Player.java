package com.gyechunsik.scoreboard.domain.football.entity;

import com.gyechunsik.scoreboard.domain.football.entity.relations.TeamPlayer;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.PlayerSquadResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

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

    @ToString.Exclude
    @OneToMany(mappedBy = "player")
    private List<TeamPlayer> teamPlayers;

    public Player(PlayerSquadResponse._PlayerData playerData) {
        this.id = playerData.getId();
        this.name = playerData.getName();
        this.photoUrl = playerData.getPhoto();
        this.position = playerData.getPosition();
    }

    public void updateFromApiData(PlayerSquadResponse._PlayerData apiData) {
        this.name = apiData.getName();
        this.photoUrl = apiData.getPhoto();
        this.position = apiData.getPosition();
    }

    public TeamPlayer toTeamPlayer(Team team) {
        return TeamPlayer.builder().player(this).team(team).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;
        return Objects.equals(getId(), player.getId()) &&
                Objects.equals(getName(), player.getName()) &&
                Objects.equals(getKoreanName(), player.getKoreanName()) &&
                Objects.equals(getPhotoUrl(), player.getPhotoUrl()) &&
                Objects.equals(getPosition(), player.getPosition());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getName());
        result = 31 * result + Objects.hashCode(getKoreanName());
        result = 31 * result + Objects.hashCode(getPhotoUrl());
        result = 31 * result + Objects.hashCode(getPosition());
        result = 31 * result + Objects.hashCode(getTeamPlayers());
        return result;
    }
}