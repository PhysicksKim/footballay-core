package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TeamInfoResponse extends ApiFootballResponse {

    private List<_TeamInfo> response;

    @Getter
    @Setter
    @ToString
    public static class _TeamInfo {
        private _TeamResponse team;
        private _Venue venue;
    }

    @Getter
    @Setter
    @ToString
    public static class _TeamResponse {
        private long id;
        private String name;
        private String code;
        private String country;
        private int founded;
        private boolean national;
        private String logo;
    }

    @Getter
    @Setter
    @ToString
    public static class _Venue {
        private long id;
        private String name;
        private String address;
        private String city;
        private int capacity;
        private String surface;
        private String image;
    }

}
