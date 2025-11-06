package com.footballay.core.domain.football.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.footballay.core.domain.football.persistence.live.*;
import com.footballay.core.domain.football.service.FixtureJobManageService;
import jakarta.persistence.*;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 축구 경기를 고유하게 특정하고 일정 및 정보를 담으며 각종 경기 데이터의 중심이 되는 엔티티 입니다. <br>
 * API 응답 중 선수의 id 가 null 인 불완전한 선수 또는 코치가 등장할 수 있는데 이를 미등록 선수(Unregistered Player) 라고 칭합니다.
 * 미등록 선수 데이터도 다루기 위해서 선수 연관관계를 맺는 곳에서는 곧바로 {@link Player} 와 연관관계를 맺지 않고 {@link MatchPlayer} 를 중간 엔티티로 활용해서 미등록 선수 문제를 해결합니다.
 * 그러므로 경기 데이터 관련 엔티티 중 선수 연관관계를 담은 엔티티를 삭제해야 한다면 foreign key constraint violation 문제가 발생하지 않도록 삭제 순서에 주의해야 합니다.
 * 예를 들어 {@link MatchPlayer} 를 사용하는 경우 삭제 시 연관관계에 주의하여 순서대로 삭제해야합니다.
 * 반면 {@link LiveStatus} 의 경우 {@link Fixture} 와 단순한 1대1 연관관계를 맺으므로 삭제 시 순서를 신경쓰지 않아도 됩니다.
 * {@link Fixture#available} 이 true 로 지정 되는 경우 {@link FixtureJobManageService} 를 거쳐 {@link org.quartz.Job} 이 등록됩니다.
 * {@link org.quartz.Job} 은 경기가 진행되는 동안 라이브로 정보를 업데이트 받아 각종 라이브 데이터를 생성합니다.
 * 클라이언트 측에서는 요청한 {@link Fixture#available} 설정 변경을 처리하려면 {@link FixtureJobManageService} 를 통해 변경이 이루어 져야지 {@link org.quartz.Job} 무결성에 문제가 생기지 않습니다.
 * 라이브 데이터들은 대체로 {@link MatchPlayer} 와 연관관계를 가지며, API 응답 데이터의 불안정함과 및 미등록 선수 문제로 인해 발생할 수 있는 예외에 주의하여야 합니다.
 *
 * @see MatchPlayer
 */
@Entity
@Table(name = "fixtures")
public class Fixture {
    @Id
    private long fixtureId;
    /**
     * referee 는 fixture 정보 게시보다는 느리고, Lineup 정보 보다는 빠른 시점에 업데이트 됩니다.
     * 따라서 fixtures 를 cache 하는 시점에 referee 는 있을 수도, 없을 수도 있습니다.
     */
    @Column(nullable = true)
    private String referee;
    private LocalDateTime date;
    private String timezone;
    private Long timestamp;
    private boolean available;
    private boolean isSummerTime;
    private String round;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_status_id")
    private LiveStatus liveStatus;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;
    @OneToMany(mappedBy = "fixture", fetch = FetchType.LAZY)
    private List<MatchLineup> lineups;
    @OneToMany(mappedBy = "fixture", fetch = FetchType.LAZY)
    private List<FixtureEvent> events;
    @OneToMany(mappedBy = "fixture", fetch = FetchType.LAZY)
    private List<TeamStatistics> teamStatistics;

    public void updateCompare(Fixture other) {
        if (!Objects.equals(this.fixtureId, other.getFixtureId())) return;
        this.referee = other.getReferee();
        this.timezone = other.getTimezone();
        this.date = other.getDate();
        this.timestamp = other.getTimestamp();
        if (!StringUtils.hasText(this.round)) {
            this.round = other.getRound();
        }
    }

    public OffsetDateTime getDateAsOffsetDateTime() {
        return ZonedDateTime.of(date, ZoneId.of(timezone)).toOffsetDateTime();
    }

    @Override
    public String toString() {
        return "Fixture{" + "fixtureId=" + fixtureId + ", referee=\'" + referee + '\'' + ", timezone=\'" + timezone + '\'' + ", date=" + date + ", timestamp=" + timestamp + ", leagueId=" + league.getLeagueId() + ", homeTeamId=" + homeTeam.getId() + ", awayTeamId=" + awayTeam.getId() + ", available=" + available + '}';
    }

    private static boolean $default$available() {
        return false;
    }

    private static boolean $default$isSummerTime() {
        return false;
    }

    private static String $default$round() {
        return "";
    }


    public static class FixtureBuilder {
        private long fixtureId;
        private String referee;
        private LocalDateTime date;
        private String timezone;
        private Long timestamp;
        private boolean available$set;
        private boolean available$value;
        private boolean isSummerTime$set;
        private boolean isSummerTime$value;
        private boolean round$set;
        private String round$value;
        private LiveStatus liveStatus;
        private League league;
        private Team homeTeam;
        private Team awayTeam;
        private List<MatchLineup> lineups;
        private List<FixtureEvent> events;
        private List<TeamStatistics> teamStatistics;

        FixtureBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder fixtureId(final long fixtureId) {
            this.fixtureId = fixtureId;
            return this;
        }

        /**
         * referee 는 fixture 정보 게시보다는 느리고, Lineup 정보 보다는 빠른 시점에 업데이트 됩니다.
         * 따라서 fixtures 를 cache 하는 시점에 referee 는 있을 수도, 없을 수도 있습니다.
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder referee(final String referee) {
            this.referee = referee;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder date(final LocalDateTime date) {
            this.date = date;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder timezone(final String timezone) {
            this.timezone = timezone;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder timestamp(final Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder available(final boolean available) {
            this.available$value = available;
            available$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder isSummerTime(final boolean isSummerTime) {
            this.isSummerTime$value = isSummerTime;
            isSummerTime$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder round(final String round) {
            this.round$value = round;
            round$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder liveStatus(final LiveStatus liveStatus) {
            this.liveStatus = liveStatus;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public Fixture.FixtureBuilder league(final League league) {
            this.league = league;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public Fixture.FixtureBuilder homeTeam(final Team homeTeam) {
            this.homeTeam = homeTeam;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @JsonIgnore
        public Fixture.FixtureBuilder awayTeam(final Team awayTeam) {
            this.awayTeam = awayTeam;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder lineups(final List<MatchLineup> lineups) {
            this.lineups = lineups;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder events(final List<FixtureEvent> events) {
            this.events = events;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public Fixture.FixtureBuilder teamStatistics(final List<TeamStatistics> teamStatistics) {
            this.teamStatistics = teamStatistics;
            return this;
        }

        public Fixture build() {
            boolean available$value = this.available$value;
            if (!this.available$set) available$value = Fixture.$default$available();
            boolean isSummerTime$value = this.isSummerTime$value;
            if (!this.isSummerTime$set) isSummerTime$value = Fixture.$default$isSummerTime();
            String round$value = this.round$value;
            if (!this.round$set) round$value = Fixture.$default$round();
            return new Fixture(this.fixtureId, this.referee, this.date, this.timezone, this.timestamp, available$value, isSummerTime$value, round$value, this.liveStatus, this.league, this.homeTeam, this.awayTeam, this.lineups, this.events, this.teamStatistics);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "Fixture.FixtureBuilder(fixtureId=" + this.fixtureId + ", referee=" + this.referee + ", date=" + this.date + ", timezone=" + this.timezone + ", timestamp=" + this.timestamp + ", available$value=" + this.available$value + ", isSummerTime$value=" + this.isSummerTime$value + ", round$value=" + this.round$value + ", liveStatus=" + this.liveStatus + ", league=" + this.league + ", homeTeam=" + this.homeTeam + ", awayTeam=" + this.awayTeam + ", lineups=" + this.lineups + ", events=" + this.events + ", teamStatistics=" + this.teamStatistics + ")";
        }
    }

    public static Fixture.FixtureBuilder builder() {
        return new Fixture.FixtureBuilder();
    }

    public long getFixtureId() {
        return this.fixtureId;
    }

    /**
     * referee 는 fixture 정보 게시보다는 느리고, Lineup 정보 보다는 빠른 시점에 업데이트 됩니다.
     * 따라서 fixtures 를 cache 하는 시점에 referee 는 있을 수도, 없을 수도 있습니다.
     */
    public String getReferee() {
        return this.referee;
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public boolean isSummerTime() {
        return this.isSummerTime;
    }

    public String getRound() {
        return this.round;
    }

    public LiveStatus getLiveStatus() {
        return this.liveStatus;
    }

    public League getLeague() {
        return this.league;
    }

    public Team getHomeTeam() {
        return this.homeTeam;
    }

    public Team getAwayTeam() {
        return this.awayTeam;
    }

    public List<MatchLineup> getLineups() {
        return this.lineups;
    }

    public List<FixtureEvent> getEvents() {
        return this.events;
    }

    public List<TeamStatistics> getTeamStatistics() {
        return this.teamStatistics;
    }

    public void setFixtureId(final long fixtureId) {
        this.fixtureId = fixtureId;
    }

    /**
     * referee 는 fixture 정보 게시보다는 느리고, Lineup 정보 보다는 빠른 시점에 업데이트 됩니다.
     * 따라서 fixtures 를 cache 하는 시점에 referee 는 있을 수도, 없을 수도 있습니다.
     */
    public void setReferee(final String referee) {
        this.referee = referee;
    }

    public void setDate(final LocalDateTime date) {
        this.date = date;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAvailable(final boolean available) {
        this.available = available;
    }

    public void setSummerTime(final boolean isSummerTime) {
        this.isSummerTime = isSummerTime;
    }

    public void setRound(final String round) {
        this.round = round;
    }

    public void setLiveStatus(final LiveStatus liveStatus) {
        this.liveStatus = liveStatus;
    }

    @JsonIgnore
    public void setLeague(final League league) {
        this.league = league;
    }

    @JsonIgnore
    public void setHomeTeam(final Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    @JsonIgnore
    public void setAwayTeam(final Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public void setLineups(final List<MatchLineup> lineups) {
        this.lineups = lineups;
    }

    public void setEvents(final List<FixtureEvent> events) {
        this.events = events;
    }

    public void setTeamStatistics(final List<TeamStatistics> teamStatistics) {
        this.teamStatistics = teamStatistics;
    }

    public Fixture() {
        this.available = Fixture.$default$available();
        this.isSummerTime = Fixture.$default$isSummerTime();
        this.round = Fixture.$default$round();
    }

    /**
     * Creates a new {@code Fixture} instance.
     *
     * @param fixtureId
     * @param referee referee 는 fixture 정보 게시보다는 느리고, Lineup 정보 보다는 빠른 시점에 업데이트 됩니다.
     * 따라서 fixtures 를 cache 하는 시점에 referee 는 있을 수도, 없을 수도 있습니다.
     * @param date
     * @param timezone
     * @param timestamp
     * @param available
     * @param isSummerTime
     * @param round
     * @param liveStatus
     * @param league
     * @param homeTeam
     * @param awayTeam
     * @param lineups
     * @param events
     * @param teamStatistics
     */
    public Fixture(final long fixtureId, final String referee, final LocalDateTime date, final String timezone, final Long timestamp, final boolean available, final boolean isSummerTime, final String round, final LiveStatus liveStatus, final League league, final Team homeTeam, final Team awayTeam, final List<MatchLineup> lineups, final List<FixtureEvent> events, final List<TeamStatistics> teamStatistics) {
        this.fixtureId = fixtureId;
        this.referee = referee;
        this.date = date;
        this.timezone = timezone;
        this.timestamp = timestamp;
        this.available = available;
        this.isSummerTime = isSummerTime;
        this.round = round;
        this.liveStatus = liveStatus;
        this.league = league;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.lineups = lineups;
        this.events = events;
        this.teamStatistics = teamStatistics;
    }
}
