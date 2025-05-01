package com.footballay.core.domain.football.external.fetch.response;

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
        private Integer number; // 선수 등번호는 시즌 전에 캐싱하는 경우 null 이 될 수 있으므로, Wrapper Class 로 선언합니다
        private String position;
        private String photo;
    }
}
