package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerInfoResponse {

    private List<_Response> response;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Response {
        private _Player player;
        private List<_Statistics> statistics;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Player {
        private Integer id;
        private String name;
        private String firstname;
        private String lastname;
        private Integer age;
        private _Birth birth;
        private String nationality;
        private String height;
        private String weight;
        private Boolean injured;
        private String photo;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Birth {
        private String date;
        private String place;
        private String country;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Statistics {
        private _Team team;
        private _League league;
        private _Games games;
        private _Substitutes substitutes;
        private _Shots shots;
        private _Goals goals;
        private _Passes passes;
        private _Tackles tackles;
        private _Duels duels;
        private _Dribbles dribbles;
        private _Fouls fouls;
        private _Cards cards;
        private _Penalty penalty;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Team {
        private Integer id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _League {
        private Integer id;
        private String name;
        private String country;
        private String logo;
        private String flag;
        private Integer season;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Games {
        private Integer appearences;
        private Integer lineups;
        private Integer minutes;
        private Integer number;
        private String position;
        private String rating;
        private Boolean captain;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Substitutes {
        private Integer in;
        private Integer out;
        private Integer bench;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Shots {
        private Integer total;
        private Integer on;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Goals {
        private Integer total;
        private Integer conceded;
        private Integer assists;
        private Integer saves;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Passes {
        private Integer total;
        private Integer key;
        private Integer accuracy;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Tackles {
        private Integer total;
        private Integer blocks;
        private Integer interceptions;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Duels {
        private Integer total;
        private Integer won;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Dribbles {
        private Integer attempts;
        private Integer success;
        private Integer past;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Fouls {
        private Integer drawn;
        private Integer committed;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Cards {
        private Integer yellow;
        private Integer yellowred;
        private Integer red;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Penalty {
        private Integer won;
        private Integer commited;
        private Integer scored;
        private Integer missed;
        private Integer saved;
    }
}