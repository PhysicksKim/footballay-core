package com.footballay.core.domain.football.persistence.relations;

import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Team;
import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"league_id", "team_id"}))
public class LeagueTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Override
    public String toString() {
        return "LeagueTeam{" + "league=" + league + ", team=" + team + '}';
    }


    public static class LeagueTeamBuilder {
        private Long id;
        private League league;
        private Team team;

        LeagueTeamBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public LeagueTeam.LeagueTeamBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LeagueTeam.LeagueTeamBuilder league(final League league) {
            this.league = league;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public LeagueTeam.LeagueTeamBuilder team(final Team team) {
            this.team = team;
            return this;
        }

        public LeagueTeam build() {
            return new LeagueTeam(this.id, this.league, this.team);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "LeagueTeam.LeagueTeamBuilder(id=" + this.id + ", league=" + this.league + ", team=" + this.team + ")";
        }
    }

    public static LeagueTeam.LeagueTeamBuilder builder() {
        return new LeagueTeam.LeagueTeamBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public League getLeague() {
        return this.league;
    }

    public Team getTeam() {
        return this.team;
    }

    public LeagueTeam() {
    }

    public LeagueTeam(final Long id, final League league, final Team team) {
        this.id = id;
        this.league = league;
        this.team = team;
    }
}
