package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 리그 순위를 제공합니다. <br>
 * 응답의 구조는 동일하지만 리그별로 group, description 가 어떻게 들어오는지 파악해야 해야합니다. <br>
 * 
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandingsResponse extends ApiFootballResponse {

    public _StandingResponseData getStandingData() {
        return response.get(0).getLeague();
    }

    private List<_LeagueStandingResponse> response;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _LeagueStandingResponse {
        private _StandingResponseData league;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _StandingResponseData {
        private int id;
        private String name;
        private String country;
        /**
         * 리그 로고 이미지 URL
         */
        private String logo;
        /**
         * 리그 국가 이미지 URL
         */
        private String flag;
        private int season;
        /**
         * API 응답상에 중첩 리스트로 구성되어 있어서 List<List<_Standing>> 으로 선언했습니다.
         * standing.get(0) 을 통해서 List<_Standing> 을 얻으면 됩니다.
         */
        private List<List<_Standing>> standings;

        public List<_Standing> getStandings() {
            return standings.get(0);
        }
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Standing {
        private _Team team;
        private int rank;
        private int points;
        /**
         * 득점 차이 (득점 - 실점) <br>
         */
        private int goalsDiff;
        /**
         * 토너먼트에서 group 을 구분해주기 위해 사용합니다. <br>
         * 예를 들어 group: "AFC Qualification, Round 2, Group A" <br>
         * 다만 API 응답상에는 "UEFA Europa League" 와 같이 group 이 없는 경우도 있습니다. <br>
         * group: "UEFA Europa League" <br>
         * 오히려 group 보다 description 이 더 의미있는 정보를 제공할 수 있습니다. <br>
         * description: "Promotion - Europa League (Play Offs: 1/8-finals)" <br>
         *
         */
        private String group;
        /**
         * W: Win, D: Draw, L: Lose <br>
         * index 작을수록 최신 경기 (index 0: 최신 경기)
         * Form 은 최근 5경기의 승무패를 보여주며, 시즌 초 5경기 미만인 경우 어떻게 표시될지 테스트 필요함
         */
        private String form;
        /**
         * 순위 등락을 나타냅니다. (up, down, same) <br>
         * 단, 순위 등락은 리그의 라운드 기준이 아니라 API 상에서 1시간마다 업데이트 되는 이전 응답과 비교한 결과입니다. <br>
         */
        private String status;
        /**
         * 해당 rank 로 마칠 경우 팀이 얻게되는 상태를 나타냅니다. (Champions League, Europa League, Relegation) <br>
         */
        @Nullable private String description;
        private _WinDrawLose all;
        private _WinDrawLose home;
        private _WinDrawLose away;
        /**
         * 해당 순위가 업데이트 된 시간을 나타냅니다. (ex. "2025-03-17T00:00:00+00:00") <br>
         */
        private String update;

        public OffsetDateTime getUpdate() {
            return OffsetDateTime.parse(update);
        }

        public String getUpdateRawString() {
            return update;
        }
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Team {
        private long id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _WinDrawLose {
        private int played;
        private int win;
        private int draw;
        private int lose;
        private _Goals goals;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Goals {
        /**
         * 득점
         */
        @JsonProperty("for")
        private int forGoals;
        /**
         * 실점
         */
        private int against;
    }
}
