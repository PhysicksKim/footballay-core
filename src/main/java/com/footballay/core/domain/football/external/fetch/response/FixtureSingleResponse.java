package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 하나의 fixture 를 조회할 때 응답에 맵핑하기 위해 사용합니다.
 * fixture id 하나로 조회하는 경우, 여러 fixture 들을 조회하는 경우와 다르게 event, lineups, statistics, players 등을 모두 포함한 응답을 반환합니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureSingleResponse extends ApiFootballResponse {
    private List<_FixtureSingle> response;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _FixtureSingle {
        private _Fixture fixture;
        private _League league;
        private _Teams teams;
        private _Goals goals;
        private _Score score;
        private List<_Events> events;
        private List<_Lineups> lineups;
        private List<_Statistics> statistics;
        private List<_FixturePlayers> players;

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

        public List<_Events> getEvents() {
            return this.events;
        }

        public List<_Lineups> getLineups() {
            return this.lineups;
        }

        public List<_Statistics> getStatistics() {
            return this.statistics;
        }

        public List<_FixturePlayers> getPlayers() {
            return this.players;
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

        public void setEvents(final List<_Events> events) {
            this.events = events;
        }

        public void setLineups(final List<_Lineups> lineups) {
            this.lineups = lineups;
        }

        public void setStatistics(final List<_Statistics> statistics) {
            this.statistics = statistics;
        }

        public void setPlayers(final List<_FixturePlayers> players) {
            this.players = players;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureSingleResponse._FixtureSingle(fixture=" + this.getFixture() + ", league=" + this.getLeague() + ", teams=" + this.getTeams() + ", goals=" + this.getGoals() + ", score=" + this.getScore() + ", events=" + this.getEvents() + ", lineups=" + this.getLineups() + ", statistics=" + this.getStatistics() + ", players=" + this.getPlayers() + ")";
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
            return "FixtureSingleResponse._Fixture(id=" + this.getId() + ", referee=" + this.getReferee() + ", timezone=" + this.getTimezone() + ", date=" + this.getDate() + ", timestamp=" + this.getTimestamp() + ", periods=" + this.getPeriods() + ", venue=" + this.getVenue() + ", status=" + this.getStatus() + ")";
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
            return "FixtureSingleResponse._Periods(first=" + this.getFirst() + ", second=" + this.getSecond() + ")";
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
            return "FixtureSingleResponse._Venue(id=" + this.getId() + ", name=" + this.getName() + ", city=" + this.getCity() + ")";
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
            return "FixtureSingleResponse._Status(longStatus=" + this.getLongStatus() + ", shortStatus=" + this.getShortStatus() + ", elapsed=" + this.getElapsed() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _League {
        private Long id;
        private String name;
        private String country;
        private String logo;
        private String flag;
        private String season;
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

        public String getSeason() {
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

        public void setSeason(final String season) {
            this.season = season;
        }

        public void setRound(final String round) {
            this.round = round;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureSingleResponse._League(id=" + this.getId() + ", name=" + this.getName() + ", country=" + this.getCountry() + ", logo=" + this.getLogo() + ", flag=" + this.getFlag() + ", season=" + this.getSeason() + ", round=" + this.getRound() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Teams {
        private _Home home;
        private _Away away;

        public _Home getHome() {
            return this.home;
        }

        public _Away getAway() {
            return this.away;
        }

        public void setHome(final _Home home) {
            this.home = home;
        }

        public void setAway(final _Away away) {
            this.away = away;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureSingleResponse._Teams(home=" + this.getHome() + ", away=" + this.getAway() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Home {
        private Long id;
        private String name;
        private String logo;
        private String winner;

        public Long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getLogo() {
            return this.logo;
        }

        public String getWinner() {
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

        public void setWinner(final String winner) {
            this.winner = winner;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureSingleResponse._Home(id=" + this.getId() + ", name=" + this.getName() + ", logo=" + this.getLogo() + ", winner=" + this.getWinner() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Away {
        private Long id;
        private String name;
        private String logo;
        private String winner;

        public Long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getLogo() {
            return this.logo;
        }

        public String getWinner() {
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

        public void setWinner(final String winner) {
            this.winner = winner;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureSingleResponse._Away(id=" + this.getId() + ", name=" + this.getName() + ", logo=" + this.getLogo() + ", winner=" + this.getWinner() + ")";
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
            return "FixtureSingleResponse._Goals(home=" + this.getHome() + ", away=" + this.getAway() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Score {
        private _Halftime halftime;
        private _Fulltime fulltime;
        private _Extratime extratime;
        private _Penalty penalty;


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
                return "FixtureSingleResponse._Score._Halftime(home=" + this.getHome() + ", away=" + this.getAway() + ")";
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
                return "FixtureSingleResponse._Score._Fulltime(home=" + this.getHome() + ", away=" + this.getAway() + ")";
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
                return "FixtureSingleResponse._Score._Extratime(home=" + this.getHome() + ", away=" + this.getAway() + ")";
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
                return "FixtureSingleResponse._Score._Penalty(home=" + this.getHome() + ", away=" + this.getAway() + ")";
            }
        }

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
            return "FixtureSingleResponse._Score(halftime=" + this.getHalftime() + ", fulltime=" + this.getFulltime() + ", extratime=" + this.getExtratime() + ", penalty=" + this.getPenalty() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Events {
        private _Time time;
        private _Team team;
        private _Player player;
        private _Assist assist;
        private String type;
        private String detail;
        private String comments;


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Time {
            private Integer elapsed;
            private Integer extra;

            public Integer getElapsed() {
                return this.elapsed;
            }

            public Integer getExtra() {
                return this.extra;
            }

            public void setElapsed(final Integer elapsed) {
                this.elapsed = elapsed;
            }

            public void setExtra(final Integer extra) {
                this.extra = extra;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Events._Time(elapsed=" + this.getElapsed() + ", extra=" + this.getExtra() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Team {
            private Long id;
            private String name;
            private String logo;

            public Long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
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

            public void setLogo(final String logo) {
                this.logo = logo;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Events._Team(id=" + this.getId() + ", name=" + this.getName() + ", logo=" + this.getLogo() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Player {
            private Long id;
            private String name;

            public Long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public void setId(final Long id) {
                this.id = id;
            }

            public void setName(final String name) {
                this.name = name;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Events._Player(id=" + this.getId() + ", name=" + this.getName() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Assist {
            private Long id;
            private String name;

            public Long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public void setId(final Long id) {
                this.id = id;
            }

            public void setName(final String name) {
                this.name = name;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Events._Assist(id=" + this.getId() + ", name=" + this.getName() + ")";
            }
        }

        public _Time getTime() {
            return this.time;
        }

        public _Team getTeam() {
            return this.team;
        }

        public _Player getPlayer() {
            return this.player;
        }

        public _Assist getAssist() {
            return this.assist;
        }

        public String getType() {
            return this.type;
        }

        public String getDetail() {
            return this.detail;
        }

        public String getComments() {
            return this.comments;
        }

        public void setTime(final _Time time) {
            this.time = time;
        }

        public void setTeam(final _Team team) {
            this.team = team;
        }

        public void setPlayer(final _Player player) {
            this.player = player;
        }

        public void setAssist(final _Assist assist) {
            this.assist = assist;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public void setDetail(final String detail) {
            this.detail = detail;
        }

        public void setComments(final String comments) {
            this.comments = comments;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureSingleResponse._Events(time=" + this.getTime() + ", team=" + this.getTeam() + ", player=" + this.getPlayer() + ", assist=" + this.getAssist() + ", type=" + this.getType() + ", detail=" + this.getDetail() + ", comments=" + this.getComments() + ")";
        }
    }


    /**
     * API 측에서 홈팀, 어웨팀 해서 2개가 Array 로 들어옴
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Lineups {
        private _Team team;
        private String formation;
        private List<_StartPlayer> startXI;
        private List<_StartPlayer> substitutes;
        private _Coach coach;


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Team {
            private Long id;
            private String name;
            private String logo;
            private _Colors colors;


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Colors {
                private _Player player;
                private _Goalkeeper goalkeeper;


                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class _Player {
                    private String primary;
                    private String number;
                    private String border;

                    public String getPrimary() {
                        return this.primary;
                    }

                    public String getNumber() {
                        return this.number;
                    }

                    public String getBorder() {
                        return this.border;
                    }

                    public void setPrimary(final String primary) {
                        this.primary = primary;
                    }

                    public void setNumber(final String number) {
                        this.number = number;
                    }

                    public void setBorder(final String border) {
                        this.border = border;
                    }

                    @java.lang.Override
                    public java.lang.String toString() {
                        return "FixtureSingleResponse._Lineups._Team._Colors._Player(primary=" + this.getPrimary() + ", number=" + this.getNumber() + ", border=" + this.getBorder() + ")";
                    }
                }


                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class _Goalkeeper {
                    private String primary;
                    private String number;
                    private String border;

                    public String getPrimary() {
                        return this.primary;
                    }

                    public String getNumber() {
                        return this.number;
                    }

                    public String getBorder() {
                        return this.border;
                    }

                    public void setPrimary(final String primary) {
                        this.primary = primary;
                    }

                    public void setNumber(final String number) {
                        this.number = number;
                    }

                    public void setBorder(final String border) {
                        this.border = border;
                    }

                    @java.lang.Override
                    public java.lang.String toString() {
                        return "FixtureSingleResponse._Lineups._Team._Colors._Goalkeeper(primary=" + this.getPrimary() + ", number=" + this.getNumber() + ", border=" + this.getBorder() + ")";
                    }
                }

                public _Player getPlayer() {
                    return this.player;
                }

                public _Goalkeeper getGoalkeeper() {
                    return this.goalkeeper;
                }

                public void setPlayer(final _Player player) {
                    this.player = player;
                }

                public void setGoalkeeper(final _Goalkeeper goalkeeper) {
                    this.goalkeeper = goalkeeper;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._Lineups._Team._Colors(player=" + this.getPlayer() + ", goalkeeper=" + this.getGoalkeeper() + ")";
                }
            }

            public Long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public String getLogo() {
                return this.logo;
            }

            public _Colors getColors() {
                return this.colors;
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

            public void setColors(final _Colors colors) {
                this.colors = colors;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Lineups._Team(id=" + this.getId() + ", name=" + this.getName() + ", logo=" + this.getLogo() + ", colors=" + this.getColors() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Coach {
            private Long id;
            private String name;
            private String photo;

            public Long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public String getPhoto() {
                return this.photo;
            }

            public void setId(final Long id) {
                this.id = id;
            }

            public void setName(final String name) {
                this.name = name;
            }

            public void setPhoto(final String photo) {
                this.photo = photo;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Lineups._Coach(id=" + this.getId() + ", name=" + this.getName() + ", photo=" + this.getPhoto() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _StartXI {
            private _Player player;

            public _Player getPlayer() {
                return this.player;
            }

            public void setPlayer(final _Player player) {
                this.player = player;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Lineups._StartXI(player=" + this.getPlayer() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _StartPlayer {
            private _Player player;

            public _Player getPlayer() {
                return this.player;
            }

            public void setPlayer(final _Player player) {
                this.player = player;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Lineups._StartPlayer(player=" + this.getPlayer() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Player {
            private Long id;
            private String name;
            private Integer number;
            private String pos;
            private String grid;

            public Long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public Integer getNumber() {
                return this.number;
            }

            public String getPos() {
                return this.pos;
            }

            public String getGrid() {
                return this.grid;
            }

            public void setId(final Long id) {
                this.id = id;
            }

            public void setName(final String name) {
                this.name = name;
            }

            public void setNumber(final Integer number) {
                this.number = number;
            }

            public void setPos(final String pos) {
                this.pos = pos;
            }

            public void setGrid(final String grid) {
                this.grid = grid;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Lineups._Player(id=" + this.getId() + ", name=" + this.getName() + ", number=" + this.getNumber() + ", pos=" + this.getPos() + ", grid=" + this.getGrid() + ")";
            }
        }

        public _Team getTeam() {
            return this.team;
        }

        public String getFormation() {
            return this.formation;
        }

        public List<_StartPlayer> getStartXI() {
            return this.startXI;
        }

        public List<_StartPlayer> getSubstitutes() {
            return this.substitutes;
        }

        public _Coach getCoach() {
            return this.coach;
        }

        public void setTeam(final _Team team) {
            this.team = team;
        }

        public void setFormation(final String formation) {
            this.formation = formation;
        }

        public void setStartXI(final List<_StartPlayer> startXI) {
            this.startXI = startXI;
        }

        public void setSubstitutes(final List<_StartPlayer> substitutes) {
            this.substitutes = substitutes;
        }

        public void setCoach(final _Coach coach) {
            this.coach = coach;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureSingleResponse._Lineups(team=" + this.getTeam() + ", formation=" + this.getFormation() + ", startXI=" + this.getStartXI() + ", substitutes=" + this.getSubstitutes() + ", coach=" + this.getCoach() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Statistics {
        private _Team team;
        private List<_StatisticsData> statistics;


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Team {
            private Long id;
            private String name;
            private String logo;

            public Long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
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

            public void setLogo(final String logo) {
                this.logo = logo;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Statistics._Team(id=" + this.getId() + ", name=" + this.getName() + ", logo=" + this.getLogo() + ")";
            }
        }

        // Api 에서는 "statistics"
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _StatisticsData {
            private String type;
            private String value;

            public String getType() {
                return this.type;
            }

            public String getValue() {
                return this.value;
            }

            public void setType(final String type) {
                this.type = type;
            }

            public void setValue(final String value) {
                this.value = value;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._Statistics._StatisticsData(type=" + this.getType() + ", value=" + this.getValue() + ")";
            }
        }

        public _Team getTeam() {
            return this.team;
        }

        public List<_StatisticsData> getStatistics() {
            return this.statistics;
        }

        public void setTeam(final _Team team) {
            this.team = team;
        }

        public void setStatistics(final List<_StatisticsData> statistics) {
            this.statistics = statistics;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureSingleResponse._Statistics(team=" + this.getTeam() + ", statistics=" + this.getStatistics() + ")";
        }
    }


    /**
     * API response 구조가 이상하여 주석을 남깁니다.
     * <pre>
     * - 네스팅 구조에서 'player' 라는 키워드가 3번 중첩되어서 헷갈림
     * - 바깥 players[] 는 home, away 팀을 나타냄
     * - 그 안에 players[] 는 해당 팀의 선수들을 나타냄
     * - 그 안에 player 는 정보, statistics[] 는 해당 선수의 통계를 나타냄
     * - statistics[] 는 무조건 1개의 {} 객체만 포함함
     * </pre>
     * <pre>
     * players[] : {
     *      team : {id, name, logo, update}
     *      players[] : {
     *          player : {id, name, photo}
     *          statistics[] : {
     *              games : {minutes, number, position, rating, captain, substitute}
     *              offsides
     *              shots : {total, on}
     *              goals : {total, conceded, assists, saves}
     *              passes : {total, key, accuracy}
     *              tackles : {total, blocks, interceptions}
     *              duels : {total, won}
     *              dribbles : {attempts, success, past}
     *              fouls : {drawn, committed}
     *              cards : {yellow, red}
     *              penalty : {won, commited, scored, missed, saved}
     *          }
     *      }
     * }
     * </pre>
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _FixturePlayers {
        private _Team team;
        private List<_PlayerStatistics> players;


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Team {
            private Long id;
            private String name;
            private String logo;
            private String update; // ex. "2024-06-22T04:01:16+00:00"

            public Long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public String getLogo() {
                return this.logo;
            }

            public String getUpdate() {
                return this.update;
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

            public void setUpdate(final String update) {
                this.update = update;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._FixturePlayers._Team(id=" + this.getId() + ", name=" + this.getName() + ", logo=" + this.getLogo() + ", update=" + this.getUpdate() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _PlayerStatistics {
            private _Player player;
            /**
             * API 응답에서 LIST 로 반환하지만, 실제로는 1개의 객체만 포함되어 있습니다. <br>
             * statistics.get(0) 으로 가져오면 됩니다.
             * <pre>
             * "statistics": [
             *   {
             *     "games": { ... }
             *     , ...
             *   }
             * ]
             * </pre>
             */
            private List<_Statistics> statistics;

            public _Player getPlayer() {
                return this.player;
            }

            /**
             * API 응답에서 LIST 로 반환하지만, 실제로는 1개의 객체만 포함되어 있습니다. <br>
             * statistics.get(0) 으로 가져오면 됩니다.
             * <pre>
             * "statistics": [
             *   {
             *     "games": { ... }
             *     , ...
             *   }
             * ]
             * </pre>
             */
            public List<_Statistics> getStatistics() {
                return this.statistics;
            }

            public void setPlayer(final _Player player) {
                this.player = player;
            }

            /**
             * API 응답에서 LIST 로 반환하지만, 실제로는 1개의 객체만 포함되어 있습니다. <br>
             * statistics.get(0) 으로 가져오면 됩니다.
             * <pre>
             * "statistics": [
             *   {
             *     "games": { ... }
             *     , ...
             *   }
             * ]
             * </pre>
             */
            public void setStatistics(final List<_Statistics> statistics) {
                this.statistics = statistics;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._FixturePlayers._PlayerStatistics(player=" + this.getPlayer() + ", statistics=" + this.getStatistics() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Player {
            private Long id;
            private String name;
            private String photo;

            public Long getId() {
                return this.id;
            }

            public String getName() {
                return this.name;
            }

            public String getPhoto() {
                return this.photo;
            }

            public void setId(final Long id) {
                this.id = id;
            }

            public void setName(final String name) {
                this.name = name;
            }

            public void setPhoto(final String photo) {
                this.photo = photo;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._FixturePlayers._Player(id=" + this.getId() + ", name=" + this.getName() + ", photo=" + this.getPhoto() + ")";
            }
        }


        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Statistics {
            private _Games games;
            private Integer offsides;
            private _Shots shots;
            private _Goals goals;
            private _Passes passes;
            private _Tackles tackles;
            private _Duels duels;
            private _Dribbles dribbles;
            private _Fouls fouls;
            private _Cards cards;
            private _Penalty penalty;


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Games {
                private Integer minutes;
                private Integer number;
                private String position;
                private String rating;
                private Boolean captain;
                private Boolean substitute;

                public Integer getMinutes() {
                    return this.minutes;
                }

                public Integer getNumber() {
                    return this.number;
                }

                public String getPosition() {
                    return this.position;
                }

                public String getRating() {
                    return this.rating;
                }

                public Boolean getCaptain() {
                    return this.captain;
                }

                public Boolean getSubstitute() {
                    return this.substitute;
                }

                public void setMinutes(final Integer minutes) {
                    this.minutes = minutes;
                }

                public void setNumber(final Integer number) {
                    this.number = number;
                }

                public void setPosition(final String position) {
                    this.position = position;
                }

                public void setRating(final String rating) {
                    this.rating = rating;
                }

                public void setCaptain(final Boolean captain) {
                    this.captain = captain;
                }

                public void setSubstitute(final Boolean substitute) {
                    this.substitute = substitute;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Games(minutes=" + this.getMinutes() + ", number=" + this.getNumber() + ", position=" + this.getPosition() + ", rating=" + this.getRating() + ", captain=" + this.getCaptain() + ", substitute=" + this.getSubstitute() + ")";
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Shots {
                private Integer total;
                private Integer on;

                public Integer getTotal() {
                    return this.total;
                }

                public Integer getOn() {
                    return this.on;
                }

                public void setTotal(final Integer total) {
                    this.total = total;
                }

                public void setOn(final Integer on) {
                    this.on = on;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Shots(total=" + this.getTotal() + ", on=" + this.getOn() + ")";
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Goals {
                private Integer total;
                private Integer conceded;
                private Integer assists;
                private Integer saves;

                public Integer getTotal() {
                    return this.total;
                }

                public Integer getConceded() {
                    return this.conceded;
                }

                public Integer getAssists() {
                    return this.assists;
                }

                public Integer getSaves() {
                    return this.saves;
                }

                public void setTotal(final Integer total) {
                    this.total = total;
                }

                public void setConceded(final Integer conceded) {
                    this.conceded = conceded;
                }

                public void setAssists(final Integer assists) {
                    this.assists = assists;
                }

                public void setSaves(final Integer saves) {
                    this.saves = saves;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Goals(total=" + this.getTotal() + ", conceded=" + this.getConceded() + ", assists=" + this.getAssists() + ", saves=" + this.getSaves() + ")";
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Passes {
                private Integer total;
                private Integer key;
                private Integer accuracy;

                public Integer getTotal() {
                    return this.total;
                }

                public Integer getKey() {
                    return this.key;
                }

                public Integer getAccuracy() {
                    return this.accuracy;
                }

                public void setTotal(final Integer total) {
                    this.total = total;
                }

                public void setKey(final Integer key) {
                    this.key = key;
                }

                public void setAccuracy(final Integer accuracy) {
                    this.accuracy = accuracy;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Passes(total=" + this.getTotal() + ", key=" + this.getKey() + ", accuracy=" + this.getAccuracy() + ")";
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Tackles {
                private Integer total;
                private Integer blocks;
                private Integer interceptions;

                public Integer getTotal() {
                    return this.total;
                }

                public Integer getBlocks() {
                    return this.blocks;
                }

                public Integer getInterceptions() {
                    return this.interceptions;
                }

                public void setTotal(final Integer total) {
                    this.total = total;
                }

                public void setBlocks(final Integer blocks) {
                    this.blocks = blocks;
                }

                public void setInterceptions(final Integer interceptions) {
                    this.interceptions = interceptions;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Tackles(total=" + this.getTotal() + ", blocks=" + this.getBlocks() + ", interceptions=" + this.getInterceptions() + ")";
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Duels {
                private Integer total;
                private Integer won;

                public Integer getTotal() {
                    return this.total;
                }

                public Integer getWon() {
                    return this.won;
                }

                public void setTotal(final Integer total) {
                    this.total = total;
                }

                public void setWon(final Integer won) {
                    this.won = won;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Duels(total=" + this.getTotal() + ", won=" + this.getWon() + ")";
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Dribbles {
                private Integer attempts;
                private Integer success;
                private Integer past;

                public Integer getAttempts() {
                    return this.attempts;
                }

                public Integer getSuccess() {
                    return this.success;
                }

                public Integer getPast() {
                    return this.past;
                }

                public void setAttempts(final Integer attempts) {
                    this.attempts = attempts;
                }

                public void setSuccess(final Integer success) {
                    this.success = success;
                }

                public void setPast(final Integer past) {
                    this.past = past;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Dribbles(attempts=" + this.getAttempts() + ", success=" + this.getSuccess() + ", past=" + this.getPast() + ")";
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Fouls {
                private Integer drawn;
                private Integer committed;

                public Integer getDrawn() {
                    return this.drawn;
                }

                public Integer getCommitted() {
                    return this.committed;
                }

                public void setDrawn(final Integer drawn) {
                    this.drawn = drawn;
                }

                public void setCommitted(final Integer committed) {
                    this.committed = committed;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Fouls(drawn=" + this.getDrawn() + ", committed=" + this.getCommitted() + ")";
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Cards {
                private Integer yellow;
                private Integer red;

                public Integer getYellow() {
                    return this.yellow;
                }

                public Integer getRed() {
                    return this.red;
                }

                public void setYellow(final Integer yellow) {
                    this.yellow = yellow;
                }

                public void setRed(final Integer red) {
                    this.red = red;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Cards(yellow=" + this.getYellow() + ", red=" + this.getRed() + ")";
                }
            }


            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Penalty {
                private Integer won;
                private Integer committed;
                private Integer scored;
                private Integer missed;
                private Integer saved;

                public Integer getWon() {
                    return this.won;
                }

                public Integer getCommitted() {
                    return this.committed;
                }

                public Integer getScored() {
                    return this.scored;
                }

                public Integer getMissed() {
                    return this.missed;
                }

                public Integer getSaved() {
                    return this.saved;
                }

                public void setWon(final Integer won) {
                    this.won = won;
                }

                public void setCommitted(final Integer committed) {
                    this.committed = committed;
                }

                public void setScored(final Integer scored) {
                    this.scored = scored;
                }

                public void setMissed(final Integer missed) {
                    this.missed = missed;
                }

                public void setSaved(final Integer saved) {
                    this.saved = saved;
                }

                @java.lang.Override
                public java.lang.String toString() {
                    return "FixtureSingleResponse._FixturePlayers._Statistics._Penalty(won=" + this.getWon() + ", committed=" + this.getCommitted() + ", scored=" + this.getScored() + ", missed=" + this.getMissed() + ", saved=" + this.getSaved() + ")";
                }
            }

            public _Games getGames() {
                return this.games;
            }

            public Integer getOffsides() {
                return this.offsides;
            }

            public _Shots getShots() {
                return this.shots;
            }

            public _Goals getGoals() {
                return this.goals;
            }

            public _Passes getPasses() {
                return this.passes;
            }

            public _Tackles getTackles() {
                return this.tackles;
            }

            public _Duels getDuels() {
                return this.duels;
            }

            public _Dribbles getDribbles() {
                return this.dribbles;
            }

            public _Fouls getFouls() {
                return this.fouls;
            }

            public _Cards getCards() {
                return this.cards;
            }

            public _Penalty getPenalty() {
                return this.penalty;
            }

            public void setGames(final _Games games) {
                this.games = games;
            }

            public void setOffsides(final Integer offsides) {
                this.offsides = offsides;
            }

            public void setShots(final _Shots shots) {
                this.shots = shots;
            }

            public void setGoals(final _Goals goals) {
                this.goals = goals;
            }

            public void setPasses(final _Passes passes) {
                this.passes = passes;
            }

            public void setTackles(final _Tackles tackles) {
                this.tackles = tackles;
            }

            public void setDuels(final _Duels duels) {
                this.duels = duels;
            }

            public void setDribbles(final _Dribbles dribbles) {
                this.dribbles = dribbles;
            }

            public void setFouls(final _Fouls fouls) {
                this.fouls = fouls;
            }

            public void setCards(final _Cards cards) {
                this.cards = cards;
            }

            public void setPenalty(final _Penalty penalty) {
                this.penalty = penalty;
            }

            @java.lang.Override
            public java.lang.String toString() {
                return "FixtureSingleResponse._FixturePlayers._Statistics(games=" + this.getGames() + ", offsides=" + this.getOffsides() + ", shots=" + this.getShots() + ", goals=" + this.getGoals() + ", passes=" + this.getPasses() + ", tackles=" + this.getTackles() + ", duels=" + this.getDuels() + ", dribbles=" + this.getDribbles() + ", fouls=" + this.getFouls() + ", cards=" + this.getCards() + ", penalty=" + this.getPenalty() + ")";
            }
        }

        public _Team getTeam() {
            return this.team;
        }

        public List<_PlayerStatistics> getPlayers() {
            return this.players;
        }

        public void setTeam(final _Team team) {
            this.team = team;
        }

        public void setPlayers(final List<_PlayerStatistics> players) {
            this.players = players;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureSingleResponse._FixturePlayers(team=" + this.getTeam() + ", players=" + this.getPlayers() + ")";
        }
    }

    public List<_FixtureSingle> getResponse() {
        return this.response;
    }

    public void setResponse(final List<_FixtureSingle> response) {
        this.response = response;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "FixtureSingleResponse(response=" + this.getResponse() + ")";
    }
}
