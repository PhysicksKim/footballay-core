package com.gyechunsik.scoreboard.domain.football.persistence.relations;


import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "player_id"}))
public class TeamPlayer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team; // 복합 키의 일부로 사용될 필드

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player; // 복합 키의 일부로 사용될 필드

    @Override
    public String toString() {
        return "TeamPlayer{" +
                "team=" + team +
                ", player=" + player +
                '}';
    }
}
