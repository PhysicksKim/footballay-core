package com.gyechunsik.scoreboard.domain.football.data.fetch.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
public class LeagueInfoResponse extends ApiFootballResponse {

    private List<Response> response;

    @Getter
    @Setter
    @ToString
    public static class Response {
        private LeagueResponse league;
        private Country country;
        private List<Season> seasons;
    }

    @Getter
    @Setter
    @ToString
    public static class LeagueResponseInner extends LeagueResponse {
    }

    @Getter
    @Setter
    @ToString
    public static class Country {
        private String name;
        private String code;
        @JsonProperty("flag")
        private String flagUrl;
    }

    @Getter
    @Setter
    @ToString
    public static class Season {
        private int year;
        private String start;
        private String end;
        private boolean current;
        private Coverage coverage;
    }

    @Getter
    @Setter
    @ToString
    public static class Coverage {
        private FixturesSupport fixtures;
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
    public static class FixturesSupport {
        private boolean events;
        private boolean lineups;
        @JsonProperty("statistics_fixtures")
        private boolean statisticsFixtures;
        @JsonProperty("statistics_players")
        private boolean statisticsPlayers;
    }
}