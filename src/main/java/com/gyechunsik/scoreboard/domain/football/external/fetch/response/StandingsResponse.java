package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandingsResponse extends ApiFootballResponse {

    private List<_LeagueStandingResponse> response;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _LeagueStandingResponse {
        private _League league;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _League {
        private int id;
        private String name;
        private String country;
        /**
         * 리그 로고 이미지 URL
         */
        private String logo;
        /**
         * 리그 국가 이미지 URL
         */
        private String flag;
        private int season;
        private List<List<_Standing>> standings;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Standing {
        private int rank;
        private _Team team;
        private int points;
        private String group;
        /**
         * W: Win, D: Draw, L: Lose
         * index 작을수록 최신 경기 (index 0: 최신 경기)
         */
        private String form;
        private String status;
        private String description;
        private _All all;
        private _Home home;
        private _Away away;
        private String update;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Team {
        private int id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _All {
        private int played;
        private int win;
        private int draw;
        private int lose;
        private _Goals goals;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Home {
        private int played;
        private int win;
        private int draw;
        private int lose;
        private _Goals goals;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Away {
        private int played;
        private int win;
        private int draw;
        private int lose;
        private _Goals goals;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Goals {
        /**
         * 득점
         */
        @JsonProperty("for")
        private int forGoals;
        /**
         * 실점
         */
        private int against;
    }
}
