package com.footballay.core.domain.football.external.fetch.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LeagueTeamsInfoResponse extends ApiFootballResponse {
    private List<_Response> response;

    @Getter
    @Setter
    public static class _Response {
        private _TeamResponse team;
        private _Venue venue;
    }

    @Getter
    @Setter
    public static class _TeamResponse {
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
    public static class _Venue {
        private int id;
        private String name;
        private String address;
        private String city;
        private int capacity;
        private String surface;
        private String image;
    }
}