package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the response structure of the fixture API. <br>
 * <h3>JSON Structure</h3>
 * <pre>
 * {
 *   "get": "fixtures",
 *   "parameters": { "league": "4", "season": "2024" },
 *   "errors": [],
 *   "results": 1,
 *   "paging": { "current": 1, "total": 1 },
 *   "response": [
 *     {
 *       "fixture": {
 *         "id": 1145509,
 *         "referee": null,
 *         "timezone": "UTC",
 *         "date": "2024-06-14T19:00:00+00:00",
 *         "timestamp": 1718391600,
 *         "periods": { "first": null, "second": null },
 *         "venue": { "id": 20732, "name": "Fußball Arena München", "city": "München" },
 *         "status": { "long": "Not Started", "short": "NS", "elapsed": null }
 *       },
 *       "league": {
 *          ...
 *       },
 *       "teams": {
 *          ...
 *       },
 *       "goals": {
 *          ...
 *        },
 *       "score": {
 *          ...
 *       }
 *     }
 *   ]
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureResponse extends ApiFootballResponse {
    private List<_Response> response;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Response {
        private _Fixture fixture;
        private _League league;
        private _Teams teams;
        private _Goals goals;
        private _Score score;

        public _Fixture getFixture() {
            return this.fixture;
        }

        public _League getLeague() {
            return this.league;
        }

        public _Teams getTeams() {
            return this.teams;
        }

        public _Goals getGoals() {
            return this.goals;
        }

        public _Score getScore() {
            return this.score;
        }

        public void setFixture(final _Fixture fixture) {
            this.fixture = fixture;
        }

        public void setLeague(final _League league) {
            this.league = league;
        }

        public void setTeams(final _Teams teams) {
            this.teams = teams;
        }

        public void setGoals(final _Goals goals) {
            this.goals = goals;
        }

        public void setScore(final _Score score) {
            this.score = score;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Response(fixture=" + this.getFixture() + ", league=" + this.getLeague() + ", teams=" + this.getTeams() + ", goals=" + this.getGoals() + ", score=" + this.getScore() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Fixture {
        private Long id;
        private String referee;
        private String timezone;
        private String date;
        private Long timestamp;
        private _Periods periods;
        private _Venue venue;
        private _Status status;

        public Long getId() {
            return this.id;
        }

        public String getReferee() {
            return this.referee;
        }

        public String getTimezone() {
            return this.timezone;
        }

        public String getDate() {
            return this.date;
        }

        public Long getTimestamp() {
            return this.timestamp;
        }

        public _Periods getPeriods() {
            return this.periods;
        }

        public _Venue getVenue() {
            return this.venue;
        }

        public _Status getStatus() {
            return this.status;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public void setReferee(final String referee) {
            this.referee = referee;
        }

        public void setTimezone(final String timezone) {
            this.timezone = timezone;
        }

        public void setDate(final String date) {
            this.date = date;
        }

        public void setTimestamp(final Long timestamp) {
            this.timestamp = timestamp;
        }

        public void setPeriods(final _Periods periods) {
            this.periods = periods;
        }

        public void setVenue(final _Venue venue) {
            this.venue = venue;
        }

        public void setStatus(final _Status status) {
            this.status = status;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Fixture(id=" + this.getId() + ", referee=" + this.getReferee() + ", timezone=" + this.getTimezone() + ", date=" + this.getDate() + ", timestamp=" + this.getTimestamp() + ", periods=" + this.getPeriods() + ", venue=" + this.getVenue() + ", status=" + this.getStatus() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Periods {
        private Long first;
        private Long second;

        public Long getFirst() {
            return this.first;
        }

        public Long getSecond() {
            return this.second;
        }

        public void setFirst(final Long first) {
            this.first = first;
        }

        public void setSecond(final Long second) {
            this.second = second;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Periods(first=" + this.getFirst() + ", second=" + this.getSecond() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Venue {
        private Long id;
        private String name;
        private String city;

        public Long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getCity() {
            return this.city;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setCity(final String city) {
            this.city = city;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Venue(id=" + this.getId() + ", name=" + this.getName() + ", city=" + this.getCity() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Status {
        @JsonProperty("long")
        private String longStatus;
        @JsonProperty("short")
        private String shortStatus;
        private Integer elapsed;

        public String getLongStatus() {
            return this.longStatus;
        }

        public String getShortStatus() {
            return this.shortStatus;
        }

        public Integer getElapsed() {
            return this.elapsed;
        }

        @JsonProperty("long")
        public void setLongStatus(final String longStatus) {
            this.longStatus = longStatus;
        }

        @JsonProperty("short")
        public void setShortStatus(final String shortStatus) {
            this.shortStatus = shortStatus;
        }

        public void setElapsed(final Integer elapsed) {
            this.elapsed = elapsed;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Status(longStatus=" + this.getLongStatus() + ", shortStatus=" + this.getShortStatus() + ", elapsed=" + this.getElapsed() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _League {
        private Long id;
        private String name;
        private String country;
        private String logo;
        private String flag;
        private Integer season;
        private String round;

        public Long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getCountry() {
            return this.country;
        }

        public String getLogo() {
            return this.logo;
        }

        public String getFlag() {
            return this.flag;
        }

        public Integer getSeason() {
            return this.season;
        }

        public String getRound() {
            return this.round;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setCountry(final String country) {
            this.country = country;
        }

        public void setLogo(final String logo) {
            this.logo = logo;
        }

        public void setFlag(final String flag) {
            this.flag = flag;
        }

        public void setSeason(final Integer season) {
            this.season = season;
        }

        public void setRound(final String round) {
            this.round = round;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._League(id=" + this.getId() + ", name=" + this.getName() + ", country=" + this.getCountry() + ", logo=" + this.getLogo() + ", flag=" + this.getFlag() + ", season=" + this.getSeason() + ", round=" + this.getRound() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Teams {
        private _Team home;
        private _Team away;

        public _Team getHome() {
            return this.home;
        }

        public _Team getAway() {
            return this.away;
        }

        public void setHome(final _Team home) {
            this.home = home;
        }

        public void setAway(final _Team away) {
            this.away = away;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Teams(home=" + this.getHome() + ", away=" + this.getAway() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Team {
        private Long id;
        private String name;
        private String logo;
        private Boolean winner;

        public Long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getLogo() {
            return this.logo;
        }

        public Boolean getWinner() {
            return this.winner;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setLogo(final String logo) {
            this.logo = logo;
        }

        public void setWinner(final Boolean winner) {
            this.winner = winner;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Team(id=" + this.getId() + ", name=" + this.getName() + ", logo=" + this.getLogo() + ", winner=" + this.getWinner() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Goals {
        private Integer home;
        private Integer away;

        public Integer getHome() {
            return this.home;
        }

        public Integer getAway() {
            return this.away;
        }

        public void setHome(final Integer home) {
            this.home = home;
        }

        public void setAway(final Integer away) {
            this.away = away;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Goals(home=" + this.getHome() + ", away=" + this.getAway() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Score {
        private _Halftime halftime;
        private _Fulltime fulltime;
        private _Extratime extratime;
        private _Penalty penalty;

        public _Halftime getHalftime() {
            return this.halftime;
        }

        public _Fulltime getFulltime() {
            return this.fulltime;
        }

        public _Extratime getExtratime() {
            return this.extratime;
        }

        public _Penalty getPenalty() {
            return this.penalty;
        }

        public void setHalftime(final _Halftime halftime) {
            this.halftime = halftime;
        }

        public void setFulltime(final _Fulltime fulltime) {
            this.fulltime = fulltime;
        }

        public void setExtratime(final _Extratime extratime) {
            this.extratime = extratime;
        }

        public void setPenalty(final _Penalty penalty) {
            this.penalty = penalty;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Score(halftime=" + this.getHalftime() + ", fulltime=" + this.getFulltime() + ", extratime=" + this.getExtratime() + ", penalty=" + this.getPenalty() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Halftime {
        private Integer home;
        private Integer away;

        public Integer getHome() {
            return this.home;
        }

        public Integer getAway() {
            return this.away;
        }

        public void setHome(final Integer home) {
            this.home = home;
        }

        public void setAway(final Integer away) {
            this.away = away;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Halftime(home=" + this.getHome() + ", away=" + this.getAway() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Fulltime {
        private Integer home;
        private Integer away;

        public Integer getHome() {
            return this.home;
        }

        public Integer getAway() {
            return this.away;
        }

        public void setHome(final Integer home) {
            this.home = home;
        }

        public void setAway(final Integer away) {
            this.away = away;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Fulltime(home=" + this.getHome() + ", away=" + this.getAway() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Extratime {
        private Integer home;
        private Integer away;

        public Integer getHome() {
            return this.home;
        }

        public Integer getAway() {
            return this.away;
        }

        public void setHome(final Integer home) {
            this.home = home;
        }

        public void setAway(final Integer away) {
            this.away = away;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Extratime(home=" + this.getHome() + ", away=" + this.getAway() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Penalty {
        private Integer home;
        private Integer away;

        public Integer getHome() {
            return this.home;
        }

        public Integer getAway() {
            return this.away;
        }

        public void setHome(final Integer home) {
            this.home = home;
        }

        public void setAway(final Integer away) {
            this.away = away;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureResponse._Penalty(home=" + this.getHome() + ", away=" + this.getAway() + ")";
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
        return "FixtureResponse(response=" + this.getResponse() + ")";
    }
}
