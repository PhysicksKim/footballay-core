package com.footballay.core.domain.football.persistence.live;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.domain.football.persistence.Team;
import jakarta.persistence.*;
import java.util.List;

/**
 * 특정 경기의 라인업 정보를 담고 있는 Entity 입니다. <br>
 * 선발 및 후보 선수들 정보를 담고있으며 각 선수들은 {@link MatchPlayer} 를 통해 선수 정보로 이어집니다. <br>
 * 드물게 무명 선수인 경우 PlayerId 가 null 이므로 직접 {@link Player} 엔티티를 참조하면 안되고
 * {@link MatchPlayer} 를 거쳐서 unregisteredPlayerName 을 사용합니다. <br>
 */
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
    @OneToMany(mappedBy = "matchLineup", fetch = FetchType.LAZY)
    private List<MatchPlayer> matchPlayers;

    @Override
    public String toString() {
        return "MatchLineup{" + "id=" + id + ", fixtureId=" + (fixture != null ? fixture.getFixtureId() : "null") + ", teamName=" + (team != null ? team.getName() : "null") + ", formation=\'" + formation + '\'' + ", matchPlayers.size=" + matchPlayers.size() + '}';
    }


    public static class MatchLineupBuilder {
        private Long id;
        private Fixture fixture;
        private Team team;
        private String formation;
        private List<MatchPlayer> matchPlayers;

        MatchLineupBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public MatchLineup.MatchLineupBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * <h3>@ManyToOne 인 이유 [ MatchLineup(n) - (1)fixture ] </h3>
         * MatchLineup 은 각각 HomeTeam MatchLineup , AwayTeam MatchLineup 으로 2개 있기 때문입니다.
         * @return {@code this}.
         */
        public MatchLineup.MatchLineupBuilder fixture(final Fixture fixture) {
            this.fixture = fixture;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchLineup.MatchLineupBuilder team(final Team team) {
            this.team = team;
            return this;
        }

        /**
         * ex. "4-2-3-1"
         * @return {@code this}.
         */
        public MatchLineup.MatchLineupBuilder formation(final String formation) {
            this.formation = formation;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchLineup.MatchLineupBuilder matchPlayers(final List<MatchPlayer> matchPlayers) {
            this.matchPlayers = matchPlayers;
            return this;
        }

        public MatchLineup build() {
            return new MatchLineup(this.id, this.fixture, this.team, this.formation, this.matchPlayers);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "MatchLineup.MatchLineupBuilder(id=" + this.id + ", fixture=" + this.fixture + ", team=" + this.team + ", formation=" + this.formation + ", matchPlayers=" + this.matchPlayers + ")";
        }
    }

    public static MatchLineup.MatchLineupBuilder builder() {
        return new MatchLineup.MatchLineupBuilder();
    }

    public MatchLineup() {
    }

    /**
     * Creates a new {@code MatchLineup} instance.
     *
     * @param id
     * @param fixture <h3>@ManyToOne 인 이유 [ MatchLineup(n) - (1)fixture ] </h3>
     * MatchLineup 은 각각 HomeTeam MatchLineup , AwayTeam MatchLineup 으로 2개 있기 때문입니다.
     * @param team
     * @param formation ex. "4-2-3-1"
     * @param matchPlayers
     */
    public MatchLineup(final Long id, final Fixture fixture, final Team team, final String formation, final List<MatchPlayer> matchPlayers) {
        this.id = id;
        this.fixture = fixture;
        this.team = team;
        this.formation = formation;
        this.matchPlayers = matchPlayers;
    }

    public Long getId() {
        return this.id;
    }

    /**
     * <h3>@ManyToOne 인 이유 [ MatchLineup(n) - (1)fixture ] </h3>
     * MatchLineup 은 각각 HomeTeam MatchLineup , AwayTeam MatchLineup 으로 2개 있기 때문입니다.
     */
    public Fixture getFixture() {
        return this.fixture;
    }

    public Team getTeam() {
        return this.team;
    }

    /**
     * ex. "4-2-3-1"
     */
    public String getFormation() {
        return this.formation;
    }

    public List<MatchPlayer> getMatchPlayers() {
        return this.matchPlayers;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * <h3>@ManyToOne 인 이유 [ MatchLineup(n) - (1)fixture ] </h3>
     * MatchLineup 은 각각 HomeTeam MatchLineup , AwayTeam MatchLineup 으로 2개 있기 때문입니다.
     */
    public void setFixture(final Fixture fixture) {
        this.fixture = fixture;
    }

    public void setTeam(final Team team) {
        this.team = team;
    }

    /**
     * ex. "4-2-3-1"
     */
    public void setFormation(final String formation) {
        this.formation = formation;
    }

    public void setMatchPlayers(final List<MatchPlayer> matchPlayers) {
        this.matchPlayers = matchPlayers;
    }
}
