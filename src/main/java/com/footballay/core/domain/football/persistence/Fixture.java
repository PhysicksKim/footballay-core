package com.footballay.core.domain.football.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.footballay.core.domain.football.persistence.live.*;
import com.footballay.core.domain.football.service.FixtureJobManageService;
import jakarta.persistence.*;
import lombok.*;
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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
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

    @Builder.Default
    private boolean available = false;

    @Builder.Default
    private boolean isSummerTime = false;

    @Builder.Default
    private String round = "";

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
        if(!StringUtils.hasText(this.round)) {
            this.round = other.getRound();
        }
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
