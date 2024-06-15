package com.gyechunsik.scoreboard.domain.football.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "fixtures")
public class Fixture {

    @Id
    private Long fixtureId;

    private String referee;
    private String timezone;
    private ZonedDateTime date;
    private Long timestamp;

    @Column(nullable = false)
    private Boolean available = false;

    @Embedded
    private Status status;

    @ManyToOne
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Status {
        private String longStatus;
        private String shortStatus;
        private Integer elapsed;
    }

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
                ", status=" + status +
                ", leagueId=" + league.getLeagueId() +
                ", homeTeamId=" + homeTeam.getId() +
                ", awayTeamId=" + awayTeam.getId() +
                '}';
    }
}
