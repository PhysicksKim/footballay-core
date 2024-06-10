package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureResponse extends ApiFootballResponse {

    private List<Response> response;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Fixture fixture;
        private League league;
        private Teams teams;
        private Goals goals;
        private Score score;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fixture {
        private Long id;
        private String referee;
        private String timezone;
        private String date;
        private Long timestamp;
        private Periods periods;
        private Venue venue;
        private Status status;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Periods {
        private Long first;
        private Long second;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Venue {
        private Long id;
        private String name;
        private String city;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        @JsonProperty("long")
        private String longStatus;
        @JsonProperty("short")
        private String shortStatus;
        private Integer elapsed;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class League {
        private Long id;
        private String name;
        private String country;
        private String logo;
        private String flag;
        private Integer season;
        private String round;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Teams {
        private Team home;
        private Team away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Team {
        private Long id;
        private String name;
        private String logo;
        private Boolean winner;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Goals {
        private Integer home;
        private Integer away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Score {
        private Halftime halftime;
        private Fulltime fulltime;
        private Extratime extratime;
        private Penalty penalty;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Halftime {
        private Integer home;
        private Integer away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fulltime {
        private Integer home;
        private Integer away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Extratime {
        private Integer home;
        private Integer away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Penalty {
        private Integer home;
        private Integer away;
    }
}
