package com.gyechunsik.scoreboard.domain.football.entity.live;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
@Entity
public class StartLineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * <h3>@ManyToOne 인 이유 [ StartLineup(n) - (1)fixture ] </h3>
     * StartLineup 은 각각 HomeTeam StartLineup , AwayTeam StartLineup 으로 2개 있기 때문입니다.
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
    private List<StartPlayer> startPlayers;

    @Override
    public String toString() {
        return "StartLineup{" +
                "id=" + id +
                ", fixtureId=" + (fixture != null ? fixture.getFixtureId() : "null") +
                ", teamName=" + (team != null ? team.getName() : "null") +
                ", formation='" + formation + '\'' +
                ", startPlayers=" + (startPlayers != null ? startPlayers.stream().map(startPlayer -> startPlayer.getPlayer().getName()).toList() : "null") +
                '}';
    }
}
