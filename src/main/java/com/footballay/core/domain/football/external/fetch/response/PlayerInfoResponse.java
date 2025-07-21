package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerInfoResponse {
    private List<_Response> response;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Response {
        private _Player player;
        private List<_Statistics> statistics;

        public _Player getPlayer() {
            return this.player;
        }

        public List<_Statistics> getStatistics() {
            return this.statistics;
        }

        public void setPlayer(final _Player player) {
            this.player = player;
        }

        public void setStatistics(final List<_Statistics> statistics) {
            this.statistics = statistics;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerInfoResponse._Response(player=" + this.getPlayer() + ", statistics=" + this.getStatistics() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Player {
        private Integer id;
        private String name;
        private String firstname;
        private String lastname;
        private Integer age;
        private _Birth birth;
        private String nationality;
        private String height;
        private String weight;
        private Boolean injured;
        private String photo;

        public Integer getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getFirstname() {
            return this.firstname;
        }

        public String getLastname() {
            return this.lastname;
        }

        public Integer getAge() {
            return this.age;
        }

        public _Birth getBirth() {
            return this.birth;
        }

        public String getNationality() {
            return this.nationality;
        }

        public String getHeight() {
            return this.height;
        }

        public String getWeight() {
            return this.weight;
        }

        public Boolean getInjured() {
            return this.injured;
        }

        public String getPhoto() {
            return this.photo;
        }

        public void setId(final Integer id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setFirstname(final String firstname) {
            this.firstname = firstname;
        }

        public void setLastname(final String lastname) {
            this.lastname = lastname;
        }

        public void setAge(final Integer age) {
            this.age = age;
        }

        public void setBirth(final _Birth birth) {
            this.birth = birth;
        }

        public void setNationality(final String nationality) {
            this.nationality = nationality;
        }

        public void setHeight(final String height) {
            this.height = height;
        }

        public void setWeight(final String weight) {
            this.weight = weight;
        }

        public void setInjured(final Boolean injured) {
            this.injured = injured;
        }

        public void setPhoto(final String photo) {
            this.photo = photo;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerInfoResponse._Player(id=" + this.getId() + ", name=" + this.getName() + ", firstname=" + this.getFirstname() + ", lastname=" + this.getLastname() + ", age=" + this.getAge() + ", birth=" + this.getBirth() + ", nationality=" + this.getNationality() + ", height=" + this.getHeight() + ", weight=" + this.getWeight() + ", injured=" + this.getInjured() + ", photo=" + this.getPhoto() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Birth {
        private String date;
        private String place;
        private String country;

        public String getDate() {
            return this.date;
        }

        public String getPlace() {
            return this.place;
        }

        public String getCountry() {
            return this.country;
        }

        public void setDate(final String date) {
            this.date = date;
        }

        public void setPlace(final String place) {
            this.place = place;
        }

        public void setCountry(final String country) {
            this.country = country;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerInfoResponse._Birth(date=" + this.getDate() + ", place=" + this.getPlace() + ", country=" + this.getCountry() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Statistics {
        private _Team team;
        private _League league;
        private _Games games;
        private _Substitutes substitutes;
        private _Shots shots;
        private _Goals goals;
        private _Passes passes;
        private _Tackles tackles;
        private _Duels duels;
        private _Dribbles dribbles;
        private _Fouls fouls;
        private _Cards cards;
        private _Penalty penalty;

        public _Team getTeam() {
            return this.team;
        }

        public _League getLeague() {
            return this.league;
        }

        public _Games getGames() {
            return this.games;
        }

        public _Substitutes getSubstitutes() {
            return this.substitutes;
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

        public void setTeam(final _Team team) {
            this.team = team;
        }

        public void setLeague(final _League league) {
            this.league = league;
        }

        public void setGames(final _Games games) {
            this.games = games;
        }

        public void setSubstitutes(final _Substitutes substitutes) {
            this.substitutes = substitutes;
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
            return "PlayerInfoResponse._Statistics(team=" + this.getTeam() + ", league=" + this.getLeague() + ", games=" + this.getGames() + ", substitutes=" + this.getSubstitutes() + ", shots=" + this.getShots() + ", goals=" + this.getGoals() + ", passes=" + this.getPasses() + ", tackles=" + this.getTackles() + ", duels=" + this.getDuels() + ", dribbles=" + this.getDribbles() + ", fouls=" + this.getFouls() + ", cards=" + this.getCards() + ", penalty=" + this.getPenalty() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Team {
        private Integer id;
        private String name;
        private String logo;

        public Integer getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getLogo() {
            return this.logo;
        }

        public void setId(final Integer id) {
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
            return "PlayerInfoResponse._Team(id=" + this.getId() + ", name=" + this.getName() + ", logo=" + this.getLogo() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _League {
        private Integer id;
        private String name;
        private String country;
        private String logo;
        private String flag;
        private Integer season;

        public Integer getId() {
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

        public void setId(final Integer id) {
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

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerInfoResponse._League(id=" + this.getId() + ", name=" + this.getName() + ", country=" + this.getCountry() + ", logo=" + this.getLogo() + ", flag=" + this.getFlag() + ", season=" + this.getSeason() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Games {
        private Integer appearences;
        private Integer lineups;
        private Integer minutes;
        private Integer number;
        private String position;
        private String rating;
        private Boolean captain;

        public Integer getAppearences() {
            return this.appearences;
        }

        public Integer getLineups() {
            return this.lineups;
        }

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

        public void setAppearences(final Integer appearences) {
            this.appearences = appearences;
        }

        public void setLineups(final Integer lineups) {
            this.lineups = lineups;
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

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerInfoResponse._Games(appearences=" + this.getAppearences() + ", lineups=" + this.getLineups() + ", minutes=" + this.getMinutes() + ", number=" + this.getNumber() + ", position=" + this.getPosition() + ", rating=" + this.getRating() + ", captain=" + this.getCaptain() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Substitutes {
        private Integer in;
        private Integer out;
        private Integer bench;

        public Integer getIn() {
            return this.in;
        }

        public Integer getOut() {
            return this.out;
        }

        public Integer getBench() {
            return this.bench;
        }

        public void setIn(final Integer in) {
            this.in = in;
        }

        public void setOut(final Integer out) {
            this.out = out;
        }

        public void setBench(final Integer bench) {
            this.bench = bench;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerInfoResponse._Substitutes(in=" + this.getIn() + ", out=" + this.getOut() + ", bench=" + this.getBench() + ")";
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
            return "PlayerInfoResponse._Shots(total=" + this.getTotal() + ", on=" + this.getOn() + ")";
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
            return "PlayerInfoResponse._Goals(total=" + this.getTotal() + ", conceded=" + this.getConceded() + ", assists=" + this.getAssists() + ", saves=" + this.getSaves() + ")";
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
            return "PlayerInfoResponse._Passes(total=" + this.getTotal() + ", key=" + this.getKey() + ", accuracy=" + this.getAccuracy() + ")";
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
            return "PlayerInfoResponse._Tackles(total=" + this.getTotal() + ", blocks=" + this.getBlocks() + ", interceptions=" + this.getInterceptions() + ")";
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
            return "PlayerInfoResponse._Duels(total=" + this.getTotal() + ", won=" + this.getWon() + ")";
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
            return "PlayerInfoResponse._Dribbles(attempts=" + this.getAttempts() + ", success=" + this.getSuccess() + ", past=" + this.getPast() + ")";
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
            return "PlayerInfoResponse._Fouls(drawn=" + this.getDrawn() + ", committed=" + this.getCommitted() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Cards {
        private Integer yellow;
        private Integer yellowred;
        private Integer red;

        public Integer getYellow() {
            return this.yellow;
        }

        public Integer getYellowred() {
            return this.yellowred;
        }

        public Integer getRed() {
            return this.red;
        }

        public void setYellow(final Integer yellow) {
            this.yellow = yellow;
        }

        public void setYellowred(final Integer yellowred) {
            this.yellowred = yellowred;
        }

        public void setRed(final Integer red) {
            this.red = red;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerInfoResponse._Cards(yellow=" + this.getYellow() + ", yellowred=" + this.getYellowred() + ", red=" + this.getRed() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Penalty {
        private Integer won;
        private Integer commited;
        private Integer scored;
        private Integer missed;
        private Integer saved;

        public Integer getWon() {
            return this.won;
        }

        public Integer getCommited() {
            return this.commited;
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

        public void setCommited(final Integer commited) {
            this.commited = commited;
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
            return "PlayerInfoResponse._Penalty(won=" + this.getWon() + ", commited=" + this.getCommited() + ", scored=" + this.getScored() + ", missed=" + this.getMissed() + ", saved=" + this.getSaved() + ")";
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
        return "PlayerInfoResponse(response=" + this.getResponse() + ")";
    }
}
