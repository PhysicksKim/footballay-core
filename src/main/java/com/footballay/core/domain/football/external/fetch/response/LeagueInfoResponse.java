package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueInfoResponse extends ApiFootballResponse {
    private List<_Response> response;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Response {
        private _LeagueResponse league;
        private _Country country;
        private List<_Season> seasons;

        public _LeagueResponse getLeague() {
            return this.league;
        }

        public _Country getCountry() {
            return this.country;
        }

        public List<_Season> getSeasons() {
            return this.seasons;
        }

        public void setLeague(final _LeagueResponse league) {
            this.league = league;
        }

        public void setCountry(final _Country country) {
            this.country = country;
        }

        public void setSeasons(final List<_Season> seasons) {
            this.seasons = seasons;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "LeagueInfoResponse._Response(league=" + this.getLeague() + ", country=" + this.getCountry() + ", seasons=" + this.getSeasons() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Country {
        private String name;
        private String code;
        @JsonProperty("flag")
        private String flagUrl;

        public String getName() {
            return this.name;
        }

        public String getCode() {
            return this.code;
        }

        public String getFlagUrl() {
            return this.flagUrl;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        @JsonProperty("flag")
        public void setFlagUrl(final String flagUrl) {
            this.flagUrl = flagUrl;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "LeagueInfoResponse._Country(name=" + this.getName() + ", code=" + this.getCode() + ", flagUrl=" + this.getFlagUrl() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Season {
        private int year;
        private String start;
        private String end;
        private boolean current;
        private _Coverage coverage;

        public int getYear() {
            return this.year;
        }

        public String getStart() {
            return this.start;
        }

        public String getEnd() {
            return this.end;
        }

        public boolean isCurrent() {
            return this.current;
        }

        public _Coverage getCoverage() {
            return this.coverage;
        }

        public void setYear(final int year) {
            this.year = year;
        }

        public void setStart(final String start) {
            this.start = start;
        }

        public void setEnd(final String end) {
            this.end = end;
        }

        public void setCurrent(final boolean current) {
            this.current = current;
        }

        public void setCoverage(final _Coverage coverage) {
            this.coverage = coverage;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "LeagueInfoResponse._Season(year=" + this.getYear() + ", start=" + this.getStart() + ", end=" + this.getEnd() + ", current=" + this.isCurrent() + ", coverage=" + this.getCoverage() + ")";
        }
    }


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

        public _FixturesSupport getFixtures() {
            return this.fixtures;
        }

        public boolean isStandings() {
            return this.standings;
        }

        public boolean isPlayers() {
            return this.players;
        }

        public boolean isTopScorers() {
            return this.topScorers;
        }

        public boolean isTopAssists() {
            return this.topAssists;
        }

        public boolean isTopCards() {
            return this.topCards;
        }

        public boolean isInjuries() {
            return this.injuries;
        }

        public boolean isPredictions() {
            return this.predictions;
        }

        public boolean isOdds() {
            return this.odds;
        }

        public void setFixtures(final _FixturesSupport fixtures) {
            this.fixtures = fixtures;
        }

        public void setStandings(final boolean standings) {
            this.standings = standings;
        }

        public void setPlayers(final boolean players) {
            this.players = players;
        }

        @JsonProperty("top_scorers")
        public void setTopScorers(final boolean topScorers) {
            this.topScorers = topScorers;
        }

        @JsonProperty("top_assists")
        public void setTopAssists(final boolean topAssists) {
            this.topAssists = topAssists;
        }

        @JsonProperty("top_cards")
        public void setTopCards(final boolean topCards) {
            this.topCards = topCards;
        }

        public void setInjuries(final boolean injuries) {
            this.injuries = injuries;
        }

        public void setPredictions(final boolean predictions) {
            this.predictions = predictions;
        }

        public void setOdds(final boolean odds) {
            this.odds = odds;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "LeagueInfoResponse._Coverage(fixtures=" + this.getFixtures() + ", standings=" + this.isStandings() + ", players=" + this.isPlayers() + ", topScorers=" + this.isTopScorers() + ", topAssists=" + this.isTopAssists() + ", topCards=" + this.isTopCards() + ", injuries=" + this.isInjuries() + ", predictions=" + this.isPredictions() + ", odds=" + this.isOdds() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _FixturesSupport {
        private boolean events;
        private boolean lineups;
        @JsonProperty("statistics_fixtures")
        private boolean statisticsFixtures;
        @JsonProperty("statistics_players")
        private boolean statisticsPlayers;

        public boolean isEvents() {
            return this.events;
        }

        public boolean isLineups() {
            return this.lineups;
        }

        public boolean isStatisticsFixtures() {
            return this.statisticsFixtures;
        }

        public boolean isStatisticsPlayers() {
            return this.statisticsPlayers;
        }

        public void setEvents(final boolean events) {
            this.events = events;
        }

        public void setLineups(final boolean lineups) {
            this.lineups = lineups;
        }

        @JsonProperty("statistics_fixtures")
        public void setStatisticsFixtures(final boolean statisticsFixtures) {
            this.statisticsFixtures = statisticsFixtures;
        }

        @JsonProperty("statistics_players")
        public void setStatisticsPlayers(final boolean statisticsPlayers) {
            this.statisticsPlayers = statisticsPlayers;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "LeagueInfoResponse._FixturesSupport(events=" + this.isEvents() + ", lineups=" + this.isLineups() + ", statisticsFixtures=" + this.isStatisticsFixtures() + ", statisticsPlayers=" + this.isStatisticsPlayers() + ")";
        }
    }

    public List<_Response> getResponse() {
        return this.response;
    }

    public void setResponse(final List<_Response> response) {
        this.response = response;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "LeagueInfoResponse(response=" + this.getResponse() + ")";
    }
}
