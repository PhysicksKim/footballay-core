package com.gyechunsik.scoreboard.domain.football.model;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.LiveStatus;
import com.gyechunsik.scoreboard.domain.football.persistence.live.PlayerStatistics;
import com.gyechunsik.scoreboard.domain.football.persistence.live.TeamStatistics;
import jakarta.annotation.Nullable;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MatchStatistics {

    private Fixture fixture;
    private LiveStatus liveStatus;
    private Team home;
    private Team away;

    @Nullable
    private TeamStatistics homeStatistics;
    @Nullable
    private TeamStatistics awayStatistics;

    private List<PlayerStatistics> homePlayerStatistics;
    private List<PlayerStatistics> awayPlayerStatistics;

    @Override
    public String toString() {
        return "MatchStatistics{" +
                "fixture.id=" + fixture.getFixtureId() +
                ", liveStatus.elapsed=" + liveStatus.getElapsed() +
                ", home.id=" + home.getId() +
                ", away.id=" + away.getId() +
                (homeStatistics != null ? ", homeStatistics=" + homeStatistics.getId() : "") +
                (awayStatistics != null ? ", awayStatistics=" + awayStatistics.getId() : "") +
                ", List<homePlayerStatistics>.size()=" + homePlayerStatistics.size() +
                ", List<awayPlayerStatistics>.size()=" + awayPlayerStatistics.size() +
                '}';
    }
}
