package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TeamInfoResponse extends ApiFootballResponse {

    private List<TeamInfo> response;

    @Getter
    @Setter
    @ToString
    public static class TeamInfo {
        private TeamResponse team;
        private Venue venue;
    }

    @Getter
    @Setter
    @ToString
    public static class TeamResponse {
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
    public static class Venue {
        private long id;
        private String name;
        private String address;
        private String city;
        private int capacity;
        private String surface;
        private String image;
    }

}
