package com.gyechunsik.scoreboard.domain.football.data.fetch.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class PlayerSquadResponse extends ApiFootballResponse {

    private List<TeamSquad> response;

    @Getter
    @Setter
    @ToString
    public static class TeamSquad {
        private ResponseTeam team;
        @ToString.Exclude
        private List<PlayerData> players;
    }

    @Getter
    @Setter
    @ToString
    public static class ResponseTeam {
        private long id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @ToString
    public static class PlayerData {
        private long id;
        private String name;
        private int age;
        private int number;
        private String position;
        private String photo;
    }
}
