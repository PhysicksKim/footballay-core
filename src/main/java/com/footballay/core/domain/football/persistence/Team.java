package com.footballay.core.domain.football.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.footballay.core.domain.football.persistence.relations.LeagueTeam;
import com.footballay.core.domain.football.persistence.relations.TeamPlayer;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "teams")
public class Team {
    @Id
    private long id;
    @Column(nullable = false)
    private String name;
    private String koreanName;
    private String logo;
    @JsonIgnore
    @OneToMany(mappedBy = "team")
    private List<LeagueTeam> leagueTeams;
    @JsonIgnore
    @OneToMany(mappedBy = "team")
    private List<TeamPlayer> teamPlayers;

    public void updateCompare(Team other) {
        if (this.id != other.getId()) return;
        if (!Objects.equals(this.name, other.getName())) this.name = other.getName();
        if (!Objects.equals(this.logo, other.getLogo())) this.logo = other.getLogo();
    }


    public static class TeamBuilder {
        private long id;
        private String name;
        private String koreanName;
        private String logo;
        private List<LeagueTeam> leagueTeams;
        private List<TeamPlayer> teamPlayers;

        TeamBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Team.TeamBuilder id(final long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Team.TeamBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Team.TeamBuilder koreanName(final String koreanName) {
            this.koreanName = koreanName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Team.TeamBuilder logo(final String logo) {
            this.logo = logo;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public Team.TeamBuilder leagueTeams(final List<LeagueTeam> leagueTeams) {
            this.leagueTeams = leagueTeams;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public Team.TeamBuilder teamPlayers(final List<TeamPlayer> teamPlayers) {
            this.teamPlayers = teamPlayers;
            return this;
        }

        public Team build() {
            return new Team(this.id, this.name, this.koreanName, this.logo, this.leagueTeams, this.teamPlayers);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "Team.TeamBuilder(id=" + this.id + ", name=" + this.name + ", koreanName=" + this.koreanName + ", logo=" + this.logo + ", leagueTeams=" + this.leagueTeams + ", teamPlayers=" + this.teamPlayers + ")";
        }
    }

    public static Team.TeamBuilder builder() {
        return new Team.TeamBuilder();
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getKoreanName() {
        return this.koreanName;
    }

    public String getLogo() {
        return this.logo;
    }

    public List<LeagueTeam> getLeagueTeams() {
        return this.leagueTeams;
    }

    public List<TeamPlayer> getTeamPlayers() {
        return this.teamPlayers;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setKoreanName(final String koreanName) {
        this.koreanName = koreanName;
    }

    public void setLogo(final String logo) {
        this.logo = logo;
    }

    @JsonIgnore
    public void setLeagueTeams(final List<LeagueTeam> leagueTeams) {
        this.leagueTeams = leagueTeams;
    }

    @JsonIgnore
    public void setTeamPlayers(final List<TeamPlayer> teamPlayers) {
        this.teamPlayers = teamPlayers;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "Team(id=" + this.getId() + ", name=" + this.getName() + ", koreanName=" + this.getKoreanName() + ", logo=" + this.getLogo() + ")";
    }

    public Team() {
    }

    public Team(final long id, final String name, final String koreanName, final String logo, final List<LeagueTeam> leagueTeams, final List<TeamPlayer> teamPlayers) {
        this.id = id;
        this.name = name;
        this.koreanName = koreanName;
        this.logo = logo;
        this.leagueTeams = leagueTeams;
        this.teamPlayers = teamPlayers;
    }
}
