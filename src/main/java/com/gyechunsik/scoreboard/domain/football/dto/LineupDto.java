package com.gyechunsik.scoreboard.domain.football.dto;

import java.util.List;
import java.util.UUID;

public record LineupDto(
        LineupTeamDto team,
        String formation,
        List<LineupPlayer> players
) {
    public record LineupTeamDto(
            long teamId,
            String name,
            String koreanName,
            String logo
    ) {
    }

    public record LineupPlayer(
            Long playerId,
            String name,
            String koreanName,
            String photoUrl,
            String position,
            Integer number,
            UUID tempId,
            String unregisteredPlayerName,
            Integer unregisteredPlayerNumber,
            String grid,
            boolean substitute
    ){
    }
}
