package com.footballay.core.domain.football.persistence.standings;

import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 리그의 시즌별 Standings 를 저장하는 Entity 입니다.
 */
@Entity
@Table(name = "standing")
public class Standing extends BaseDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = true) // Migration 동안 true
    private League league;

    private int season;

    @OneToMany(mappedBy = "standing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StandingTeam> standingTeams = new ArrayList<>();

    // Protected no-args constructor for JPA
    protected Standing() {
    }

    // Private all-args constructor for builder
    private Standing(Long id, League league, int season, List<StandingTeam> standingTeams) {
        this.id = id;
        this.league = league;
        this.season = season;
        this.standingTeams = standingTeams != null ? standingTeams : new ArrayList<>();
    }

    public void addStandingTeam(StandingTeam standingTeam) {
        standingTeams.add(standingTeam);
        standingTeam.setStanding(this);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public League getLeague() {
        return league;
    }

    public int getSeason() {
        return season;
    }

    public List<StandingTeam> getStandingTeams() {
        return standingTeams;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public void setStandingTeams(List<StandingTeam> standingTeams) {
        this.standingTeams = standingTeams;
    }

    // Builder
    public static StandingBuilder builder() {
        return new StandingBuilder();
    }

    public static class StandingBuilder {
        private Long id;
        private League league;
        private int season;
        private List<StandingTeam> standingTeams = new ArrayList<>();

        StandingBuilder() {
        }

        public StandingBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public StandingBuilder league(League league) {
            this.league = league;
            return this;
        }

        public StandingBuilder season(int season) {
            this.season = season;
            return this;
        }

        public StandingBuilder standingTeams(List<StandingTeam> standingTeams) {
            this.standingTeams = standingTeams;
            return this;
        }

        public Standing build() {
            return new Standing(id, league, season, standingTeams);
        }
    }
}