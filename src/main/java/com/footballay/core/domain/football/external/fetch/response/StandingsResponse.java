package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 리그 순위를 제공합니다. <br>
 * 응답의 구조는 동일하지만 리그별로 group, description 가 어떻게 들어오는지 파악해야 해야합니다. <br>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandingsResponse extends ApiFootballResponse {

    private List<_LeagueStandingResponse> response;

    public _StandingResponseData getStandingData() {
        return response.get(0).getLeague();
    }

    public List<_LeagueStandingResponse> getResponse() {
        return response;
    }

    public void setResponse(List<_LeagueStandingResponse> response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "StandingsResponse{" +
                "response=" + response +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _LeagueStandingResponse {
        private _StandingResponseData league;

        public _StandingResponseData getLeague() {
            return league;
        }

        public void setLeague(_StandingResponseData league) {
            this.league = league;
        }

        @Override
        public String toString() {
            return "_LeagueStandingResponse{" +
                    "league=" + league +
                    '}';
        }
    }

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

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public int getSeason() {
            return season;
        }

        public void setSeason(int season) {
            this.season = season;
        }

        public void setStandings(List<List<_Standing>> standings) {
            this.standings = standings;
        }

        @Override
        public String toString() {
            return "_StandingResponseData{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", country='" + country + '\'' +
                    ", logo='" + logo + '\'' +
                    ", flag='" + flag + '\'' +
                    ", season=" + season +
                    ", standings=" + standings +
                    '}';
        }
    }

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

        public _Team getTeam() {
            return team;
        }

        public void setTeam(_Team team) {
            this.team = team;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public int getGoalsDiff() {
            return goalsDiff;
        }

        public void setGoalsDiff(int goalsDiff) {
            this.goalsDiff = goalsDiff;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getForm() {
            return form;
        }

        public void setForm(String form) {
            this.form = form;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public _WinDrawLose getAll() {
            return all;
        }

        public void setAll(_WinDrawLose all) {
            this.all = all;
        }

        public _WinDrawLose getHome() {
            return home;
        }

        public void setHome(_WinDrawLose home) {
            this.home = home;
        }

        public _WinDrawLose getAway() {
            return away;
        }

        public void setAway(_WinDrawLose away) {
            this.away = away;
        }

        public void setUpdate(String update) {
            this.update = update;
        }

        @Override
        public String toString() {
            return "_Standing{" +
                    "team=" + team +
                    ", rank=" + rank +
                    ", points=" + points +
                    ", goalsDiff=" + goalsDiff +
                    ", group='" + group + '\'' +
                    ", form='" + form + '\'' +
                    ", status='" + status + '\'' +
                    ", description='" + description + '\'' +
                    ", all=" + all +
                    ", home=" + home +
                    ", away=" + away +
                    ", update='" + update + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Team {
        private long id;
        private String name;
        private String logo;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        @Override
        public String toString() {
            return "_Team{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", logo='" + logo + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _WinDrawLose {
        private int played;
        private int win;
        private int draw;
        private int lose;
        private _Goals goals;

        public int getPlayed() {
            return played;
        }

        public void setPlayed(int played) {
            this.played = played;
        }

        public int getWin() {
            return win;
        }

        public void setWin(int win) {
            this.win = win;
        }

        public int getDraw() {
            return draw;
        }

        public void setDraw(int draw) {
            this.draw = draw;
        }

        public int getLose() {
            return lose;
        }

        public void setLose(int lose) {
            this.lose = lose;
        }

        public _Goals getGoals() {
            return goals;
        }

        public void setGoals(_Goals goals) {
            this.goals = goals;
        }

        @Override
        public String toString() {
            return "_WinDrawLose{" +
                    "played=" + played +
                    ", win=" + win +
                    ", draw=" + draw +
                    ", lose=" + lose +
                    ", goals=" + goals +
                    '}';
        }
    }

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

        public int getForGoals() {
            return forGoals;
        }

        public void setForGoals(int forGoals) {
            this.forGoals = forGoals;
        }

        public int getAgainst() {
            return against;
        }

        public void setAgainst(int against) {
            this.against = against;
        }

        @Override
        public String toString() {
            return "_Goals{" +
                    "forGoals=" + forGoals +
                    ", against=" + against +
                    '}';
        }
    }
}
