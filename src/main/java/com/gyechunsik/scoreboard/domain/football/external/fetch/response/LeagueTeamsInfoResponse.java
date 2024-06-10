package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LeagueTeamsInfoResponse extends ApiFootballResponse {
    private List<Response> response;

    @Getter
    @Setter
    public static class Response {
        private TeamResponse team;
        private Venue venue;
    }

    @Getter
    @Setter
    public static class TeamResponse {
        private int id;
        private String name;
        private String code;
        private String country;
        private int founded;
        private boolean national;
        private String logo;
    }

    @Getter
    @Setter
    public static class Venue {
        private int id;
        private String name;
        private String address;
        private String city;
        private int capacity;
        private String surface;
        private String image;
    }
}