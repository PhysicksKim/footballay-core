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
public class LeagueInfoResponse extends ApiFootballResponse {

    private List<_Response> response;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Response {
        private _LeagueResponse league;
        private _Country country;
        private List<_Season> seasons;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Country {
        private String name;
        private String code;
        @JsonProperty("flag")
        private String flagUrl;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Season {
        private int year;
        private String start;
        private String end;
        private boolean current;
        private _Coverage coverage;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Coverage {
        private _FixturesSupport fixtures;
        private boolean standings;
        private boolean players;
        @JsonProperty("top_scorers")
        private boolean topScorers;
        @JsonProperty("top_assists")
        private boolean topAssists;
        @JsonProperty("top_cards")
        private boolean topCards;
        private boolean injuries;
        private boolean predictions;
        private boolean odds;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _FixturesSupport {
        private boolean events;
        private boolean lineups;
        @JsonProperty("statistics_fixtures")
        private boolean statisticsFixtures;
        @JsonProperty("statistics_players")
        private boolean statisticsPlayers;
    }
}