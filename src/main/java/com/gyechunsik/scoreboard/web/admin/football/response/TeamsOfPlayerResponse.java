package com.gyechunsik.scoreboard.web.admin.football.response;

public record TeamsOfPlayerResponse(
    _Player player,
    _Team[] teams
) {



    public record _Player(
            long playerId,
            String name,
            String koreanName,
            String photoUrl,
            String position
    ){}

    public record _Team(
            long teamId,
            String name,
            String koreanName,
            String logo
    ){}
}
