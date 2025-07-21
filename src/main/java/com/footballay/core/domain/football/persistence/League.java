package com.footballay.core.domain.football.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.footballay.core.domain.football.persistence.relations.LeagueTeam;
import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "leagues")
public class League extends BaseDateAuditEntity {
    @Id
    private Long leagueId;
    @Column(nullable = false)
    private String name;
    private String koreanName;
    private String logo;
    private boolean available;
    @Column(nullable = true)
    private Integer currentSeason;
    @JsonIgnore
    @OneToMany(mappedBy = "league")
    private List<LeagueTeam> leagueTeams;

    public void updateCompare(League other) {
        if (this.getLeagueId() != other.getLeagueId()) return;
        if (!Objects.equals(this.getName(), other.getName())) this.setName(other.getName());
        // 로고는 업데이트 하지 않음. 다른 로고를 사용할 수 있기 때문임
        // if(!Objects.equals(this.getLogo(), other.getLogo())) this.setLogo(other.getLogo());
        if (!Objects.equals(this.getCurrentSeason(), other.getCurrentSeason())) this.setCurrentSeason(other.getCurrentSeason());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        League league = (League) o;
        return Objects.equals(getLeagueId(), league.getLeagueId()) && Objects.equals(getName(), league.getName()) && Objects.equals(getKoreanName(), league.getKoreanName()) && Objects.equals(getLogo(), league.getLogo()) && Objects.equals(getCurrentSeason(), league.getCurrentSeason()) && Objects.equals(getLeagueTeams(), league.getLeagueTeams());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getLeagueId());
        result = 31 * result + Objects.hashCode(getName());
        result = 31 * result + Objects.hashCode(getKoreanName());
        result = 31 * result + Objects.hashCode(getLogo());
        result = 31 * result + Objects.hashCode(getCurrentSeason());
        result = 31 * result + Objects.hashCode(getLeagueTeams());
        return result;
    }

    private static boolean $default$available() {
        return false;
    }


    public static class LeagueBuilder {
        private Long leagueId;
        private String name;
        private String koreanName;
        private String logo;
        private boolean available$set;
        private boolean available$value;
        private Integer currentSeason;
        private List<LeagueTeam> leagueTeams;

        LeagueBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public League.LeagueBuilder leagueId(final Long leagueId) {
            this.leagueId = leagueId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public League.LeagueBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public League.LeagueBuilder koreanName(final String koreanName) {
            this.koreanName = koreanName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public League.LeagueBuilder logo(final String logo) {
            this.logo = logo;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public League.LeagueBuilder available(final boolean available) {
            this.available$value = available;
            available$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public League.LeagueBuilder currentSeason(final Integer currentSeason) {
            this.currentSeason = currentSeason;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public League.LeagueBuilder leagueTeams(final List<LeagueTeam> leagueTeams) {
            this.leagueTeams = leagueTeams;
            return this;
        }

        public League build() {
            boolean available$value = this.available$value;
            if (!this.available$set) available$value = League.$default$available();
            return new League(this.leagueId, this.name, this.koreanName, this.logo, available$value, this.currentSeason, this.leagueTeams);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "League.LeagueBuilder(leagueId=" + this.leagueId + ", name=" + this.name + ", koreanName=" + this.koreanName + ", logo=" + this.logo + ", available$value=" + this.available$value + ", currentSeason=" + this.currentSeason + ", leagueTeams=" + this.leagueTeams + ")";
        }
    }

    public static League.LeagueBuilder builder() {
        return new League.LeagueBuilder();
    }

    public Long getLeagueId() {
        return this.leagueId;
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

    public boolean isAvailable() {
        return this.available;
    }

    public Integer getCurrentSeason() {
        return this.currentSeason;
    }

    public List<LeagueTeam> getLeagueTeams() {
        return this.leagueTeams;
    }

    public void setLeagueId(final Long leagueId) {
        this.leagueId = leagueId;
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

    public void setAvailable(final boolean available) {
        this.available = available;
    }

    public void setCurrentSeason(final Integer currentSeason) {
        this.currentSeason = currentSeason;
    }

    @JsonIgnore
    public void setLeagueTeams(final List<LeagueTeam> leagueTeams) {
        this.leagueTeams = leagueTeams;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "League(leagueId=" + this.getLeagueId() + ", name=" + this.getName() + ", koreanName=" + this.getKoreanName() + ", logo=" + this.getLogo() + ", available=" + this.isAvailable() + ", currentSeason=" + this.getCurrentSeason() + ")";
    }

    public League() {
        this.available = League.$default$available();
    }

    public League(final Long leagueId, final String name, final String koreanName, final String logo, final boolean available, final Integer currentSeason, final List<LeagueTeam> leagueTeams) {
        this.leagueId = leagueId;
        this.name = name;
        this.koreanName = koreanName;
        this.logo = logo;
        this.available = available;
        this.currentSeason = currentSeason;
        this.leagueTeams = leagueTeams;
    }
}
