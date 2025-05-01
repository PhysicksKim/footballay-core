package com.footballay.core.domain.football.external.fetch.response;

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

    private List<_CurrentLeagueData> response;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _CurrentLeagueData {
        private _League league;
        private _Country country;
        private List<_Season> seasons;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _League {
        private Long id;
        private String name;
        private String type;
        private String logo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Country {
        private String name;
        private String code;
        private String flag;
    }

    @Getter
    @Setter
    @NoArgsConstructor
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
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Coverage {
        private _Fixtures fixtures;
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
    public static class _Fixtures {
        private boolean events;
        private boolean lineups;
        private boolean statisticsFixtures;
        private boolean statisticsPlayers;
    }
}
