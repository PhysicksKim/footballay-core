package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueCurrentResponse extends ApiFootballResponse {
    private List<_CurrentLeagueData> response;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _CurrentLeagueData {
        private _League league;
        private _Country country;
        private List<_Season> seasons;

        public _League getLeague() {
            return this.league;
        }

        public _Country getCountry() {
            return this.country;
        }

        public List<_Season> getSeasons() {
            return this.seasons;
        }

        public void setLeague(final _League league) {
            this.league = league;
        }

        public void setCountry(final _Country country) {
            this.country = country;
        }

        public void setSeasons(final List<_Season> seasons) {
            this.seasons = seasons;
        }

        public _CurrentLeagueData() {
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _League {
        private Long id;
        private String name;
        private String type;
        private String logo;

        public Long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getType() {
            return this.type;
        }

        public String getLogo() {
            return this.logo;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public void setLogo(final String logo) {
            this.logo = logo;
        }

        public _League() {
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Country {
        private String name;
        private String code;
        private String flag;

        public String getName() {
            return this.name;
        }

        public String getCode() {
            return this.code;
        }

        public String getFlag() {
            return this.flag;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public void setFlag(final String flag) {
            this.flag = flag;
        }

        public _Country() {
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

        public _Season() {
        }
    }


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

        public _Fixtures getFixtures() {
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

        public void setFixtures(final _Fixtures fixtures) {
            this.fixtures = fixtures;
        }

        public void setStandings(final boolean standings) {
            this.standings = standings;
        }

        public void setPlayers(final boolean players) {
            this.players = players;
        }

        public void setTopScorers(final boolean topScorers) {
            this.topScorers = topScorers;
        }

        public void setTopAssists(final boolean topAssists) {
            this.topAssists = topAssists;
        }

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

        public _Coverage() {
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Fixtures {
        private boolean events;
        private boolean lineups;
        private boolean statisticsFixtures;
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

        public void setStatisticsFixtures(final boolean statisticsFixtures) {
            this.statisticsFixtures = statisticsFixtures;
        }

        public void setStatisticsPlayers(final boolean statisticsPlayers) {
            this.statisticsPlayers = statisticsPlayers;
        }

        public _Fixtures() {
        }
    }

    public List<_CurrentLeagueData> getResponse() {
        return this.response;
    }

    public void setResponse(final List<_CurrentLeagueData> response) {
        this.response = response;
    }

    public LeagueCurrentResponse() {
    }
}
