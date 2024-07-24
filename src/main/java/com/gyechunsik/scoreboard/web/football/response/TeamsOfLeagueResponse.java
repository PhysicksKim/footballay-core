package com.gyechunsik.scoreboard.web.football.response;

import com.gyechunsik.scoreboard.domain.football.entity.Team;

public record TeamsOfLeagueResponse(
        long id,
        String name,
        String koreanName,
        String logo
) {
}
