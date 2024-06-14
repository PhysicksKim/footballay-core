package com.gyechunsik.scoreboard.domain.football.entity.relations;

import java.io.Serializable;
import java.util.Objects;

public class TeamPlayersId implements Serializable {

    private Long team;
    private Long player;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamPlayersId that = (TeamPlayersId) o;
        return Objects.equals(team, that.team) && Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(team);
        result = 31 * result + Objects.hashCode(player);
        return result;
    }
}
