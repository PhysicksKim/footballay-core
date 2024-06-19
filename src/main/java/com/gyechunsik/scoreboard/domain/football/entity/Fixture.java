package com.gyechunsik.scoreboard.domain.football.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Fixture {

    @Id
    private Long fixtureId;

    private String referee;
    private String timezone;
    private ZonedDateTime date;
    private Long timestamp;

    @Builder.Default
    private boolean available = false;

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

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Periods {
        private Long first;
        private Long second;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Venue {
        private Long venueId;
        private String name;
        private String city;
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
                '}';
    }
}
