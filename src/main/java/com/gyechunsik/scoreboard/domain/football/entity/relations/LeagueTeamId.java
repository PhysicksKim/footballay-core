package com.gyechunsik.scoreboard.domain.football.entity.relations;

import java.io.Serializable;
import java.util.Objects;

public class LeagueTeamId implements Serializable {

    private Long league;
    private Long team;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LeagueTeamId that = (LeagueTeamId) o;

        if (!Objects.equals(league, that.league)) return false;
        return Objects.equals(team, that.team);
    }

    @Override
    public int hashCode() {
        int result = league != null ? league.hashCode() : 0;
        result = 31 * result + (team != null ? team.hashCode() : 0);
        return result;
    }
}
