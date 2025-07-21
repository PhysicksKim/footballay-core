package com.footballay.core.domain.football.persistence.relations;

import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.domain.football.persistence.Team;
import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "player_id"}))
public class TeamPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team; // 복합 키의 일부로 사용될 필드
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player; // 복합 키의 일부로 사용될 필드

    @Override
    public String toString() {
        return "TeamPlayer{" + "team=" + team + ", player=" + player + '}';
    }


    public static class TeamPlayerBuilder {
        private Long id;
        private Team team;
        private Player player;

        TeamPlayerBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public TeamPlayer.TeamPlayerBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public TeamPlayer.TeamPlayerBuilder team(final Team team) {
            this.team = team;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public TeamPlayer.TeamPlayerBuilder player(final Player player) {
            this.player = player;
            return this;
        }

        public TeamPlayer build() {
            return new TeamPlayer(this.id, this.team, this.player);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "TeamPlayer.TeamPlayerBuilder(id=" + this.id + ", team=" + this.team + ", player=" + this.player + ")";
        }
    }

    public static TeamPlayer.TeamPlayerBuilder builder() {
        return new TeamPlayer.TeamPlayerBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public Team getTeam() {
        return this.team;
    }

    public Player getPlayer() {
        return this.player;
    }

    public TeamPlayer() {
    }

    public TeamPlayer(final Long id, final Team team, final Player player) {
        this.id = id;
        this.team = team;
        this.player = player;
    }
}
