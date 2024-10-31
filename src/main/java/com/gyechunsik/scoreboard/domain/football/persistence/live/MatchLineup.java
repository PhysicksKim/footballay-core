package com.gyechunsik.scoreboard.domain.football.persistence.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * 특정 경기의 라인업 정보를 담고 있는 Entity 입니다. <br>
 * 선발 및 후보 선수들 정보를 담고있으며 각 선수들은 {@link MatchPlayer} 를 통해 선수 정보로 이어집니다. <br>
 * 드물게 무명 선수인 경우 PlayerId 가 null 이므로 직접 {@link Player} 엔티티를 참조하면 안되고
 * {@link MatchPlayer} 를 거쳐서 unregisteredPlayerName 을 사용합니다. <br>
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
@Entity
public class MatchLineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * <h3>@ManyToOne 인 이유 [ MatchLineup(n) - (1)fixture ] </h3>
     * MatchLineup 은 각각 HomeTeam MatchLineup , AwayTeam MatchLineup 으로 2개 있기 때문입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * ex. "4-2-3-1"
     */
    private String formation;

    @OneToMany(mappedBy = "startLineup", fetch = FetchType.LAZY)
    private List<MatchPlayer> matchPlayers;

    @Override
    public String toString() {
        return "MatchLineup{" +
                "id=" + id +
                ", fixtureId=" + (fixture != null ? fixture.getFixtureId() : "null") +
                ", teamName=" + (team != null ? team.getName() : "null") +
                ", formation='" + formation + '\'' +
                ", matchPlayers=" + (matchPlayers != null ? matchPlayers.stream().map(startPlayer -> startPlayer.getPlayer().getName()).toList() : "null") +
                '}';
    }
}
