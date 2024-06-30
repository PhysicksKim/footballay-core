package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class PlayerSquadResponse extends ApiFootballResponse {

    private List<_TeamSquad> response;

    @Getter
    @Setter
    @ToString
    public static class _TeamSquad {
        private _ResponseTeam team;
        @ToString.Exclude
        private List<_PlayerData> players;
    }

    @Getter
    @Setter
    @ToString
    public static class _ResponseTeam {
        private long id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class _PlayerData {
        private long id;
        private String name;
        private int age;
        private int number;
        private String position;
        private String photo;
    }
}
