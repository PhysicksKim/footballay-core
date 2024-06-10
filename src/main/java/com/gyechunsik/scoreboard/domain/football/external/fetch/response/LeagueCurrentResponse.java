package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueCurrentResponse extends ApiFootballResponse {
    private List<CurrentLeagueData> response;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentLeagueData {
        private League league;
        private Country country;
        private List<Season> seasons;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class League {
        private Long id;
        private String name;
        private String type;
        private String logo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Country {
        private String name;
        private String code;
        private String flag;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Season {
        private int year;
        private String start;
        private String end;
        private boolean current;
        private Coverage coverage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coverage {
        private Fixtures fixtures;
        private boolean standings;
        private boolean players;
        private boolean topScorers;
        private boolean topAssists;
        private boolean topCards;
        private boolean injuries;
        private boolean predictions;
        private boolean odds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fixtures {
        private boolean events;
        private boolean lineups;
        private boolean statisticsFixtures;
        private boolean statisticsPlayers;
    }
}
