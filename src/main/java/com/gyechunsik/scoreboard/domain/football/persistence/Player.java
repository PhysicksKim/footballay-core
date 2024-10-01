package com.gyechunsik.scoreboard.domain.football.persistence;

import com.gyechunsik.scoreboard.domain.football.persistence.relations.TeamPlayer;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 선수 데이터의 기본이 되는 값들을 담습니다. <br>
 * API 전반에서는 id 를 기본으로 name 을 부가적인 정보로 사용합니다 <br>
 * 예를 들어 API 요청에서 name 으로 선수의 id 를 찾을 수도 있습니다. <br>
 * 기본 정보는 여러 API 응답에서 공통적으로 최소한으로 필요한 정보를 포함합니다. <br>
 * 예를 들어 Players/Squad Endpoint 에서 제공하는 정보가 있을 수 있고 <br>
 * Players Endpoint 에서 제공하는 정보가 있을 수 있고, Fixture/Lineups 에서 제공하는 정보가 있을 수 있습니다. <br>
 * 여러 API 응답에서 최소한으로 id, name 값이 제공되며, 경우에 따라 이외의 photoUrl, position, number 정보가 제공됩니다. <br>
 * photoUrl 은 id 를 기반으로 "https://media.api-sports.io/football/teams/{playerId}.png" 의 규칙을 가지기 때문에 id 만으로 photoUrl 값을 채워넣을 수 있습니다. <br>
 */
@Getter
@Setter
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

    /**
     * squad 또는 lineup 에서 제공되는 포지션 정보입니다. <br>
     */
    private String position;

    /**
     * 선수의 등번호 정보입니다. lineup 에서 제공됩니다 <br>
     */
    @Column(nullable = true)
    private Integer number;

    /**
     * preventUnlink == true 인 경우, API 캐싱 시에 팀과 연관관계가 끊어지지 않고 추가되기만 합니다. <br>
     * 이적, 임대, 유스콜업 등의 경우로 인해 선수가 추가되는 경우 API TEAM Squad 에는 선수가 업데이트 되지 않을 수 있습니다. <br>
     * 이 경우 수동으로 {@link TeamPlayer} relation 을 설정해주고 preventUnlink 를 true 로 설정하면 <br>
     * API Cache 과정에서 Squad Response 에 해당 선수가 없더라도 연관관계가 끊어지지 않습니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean preventUnlink = false;

    @ToString.Exclude
    @OneToMany(mappedBy = "player")
    @Builder.Default
    private List<TeamPlayer> teamPlayers = new ArrayList<>();

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

    @Override
    public String toString() {
        return "Player{" +
                "number=" + number +
                ", position='" + position + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", koreanName='" + koreanName + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}