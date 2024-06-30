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

    private List<_Response> response;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Response {
        private _Fixture fixture;
        private _League league;
        private _Teams teams;
        private _Goals goals;
        private _Score score;
    }

    @Getter
    @Setter
    @ToString
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
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Periods {
        private Long first;
        private Long second;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Venue {
        private Long id;
        private String name;
        private String city;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Status {
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
    public static class _League {
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
    public static class _Teams {
        private _Team home;
        private _Team away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Team {
        private Long id;
        private String name;
        private String logo;
        private Boolean winner;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Goals {
        private Integer home;
        private Integer away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Score {
        private _Halftime halftime;
        private _Fulltime fulltime;
        private _Extratime extratime;
        private _Penalty penalty;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Halftime {
        private Integer home;
        private Integer away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Fulltime {
        private Integer home;
        private Integer away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Extratime {
        private Integer home;
        private Integer away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Penalty {
        private Integer home;
        private Integer away;
    }
}
