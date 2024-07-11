package com.gyechunsik.scoreboard.domain.football.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartLineup;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Fixture {

    @Id
    private Long fixtureId;

    /**
     * referee 는 fixture 정보 게시보다는 느리고, Lineup 정보 보다는 빠른 시점에 업데이트 됩니다.
     * 따라서 fixtures 를 cache 하는 시점에 referee 는 있을 수도, 없을 수도 있습니다.
     */
    @Column(nullable = true)
    private String referee;

    private LocalDateTime date;
    private String timezone;
    private Long timestamp;

    @Builder.Default
    private boolean available = false;

    @Builder.Default
    private boolean isSummerTime = false;

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
    private List<StartLineup> lineups;

    @OneToMany(mappedBy = "fixture", fetch = FetchType.LAZY)
    private List<FixtureEvent> events;

    public void updateCompare(Fixture other) {
        if (!Objects.equals(this.fixtureId, other.getFixtureId())) return;
        this.referee = other.getReferee();
        this.timezone = other.getTimezone();
        this.date = other.getDate();
        this.timestamp = other.getTimestamp();
    }

    public OffsetDateTime getDateAsOffsetDateTime() {
        return ZonedDateTime.of(date, ZoneId.of(timezone)).toOffsetDateTime();
    }

    @Override
    public String toString() {
        return "Fixture{" +
                "fixtureId=" + fixtureId +
                ", referee='" + referee + '\'' +
                ", timezone='" + timezone + '\'' +
                ", date=" + date +
                ", timestamp=" + timestamp +
                ", leagueId=" + league.getLeagueId() +
                ", homeTeamId=" + homeTeam.getId() +
                ", awayTeamId=" + awayTeam.getId() +
                ", available=" + available +
                '}';
    }
}
