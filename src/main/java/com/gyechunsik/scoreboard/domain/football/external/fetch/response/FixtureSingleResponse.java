package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 하나의 fixture 를 조회할 때 응답에 맵핑하기 위해 사용합니다.
 * fixture id 하나로 조회하는 경우, 여러 fixture 들을 조회하는 경우와 다르게 event, lineups, statistics, players 등을 모두 포함한 응답을 반환합니다.
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureSingleResponse extends ApiFootballResponse {

    private List<FixtureSingle> response;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FixtureSingle {
        private Fixture fixture;
        private League league;
        private Teams teams;
        private Goals goals;
        private Score score;
        private List<Events> events;
        private List<Lineups> lineups;
        private List<Statistics> statistics;
        private List<FixturePlayers> players;
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
        private String elapsed;
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
        private String season;
        private String round;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Teams {
        private Home home;
        private Away away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Home {
        private Long id;
        private String name;
        private String logo;
        private String winner;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Away {
        private Long id;
        private String name;
        private String logo;
        private String winner;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Goals {
        private Long home;
        private Long away;
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

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Events {
        private Time time;
        private Team team;
        private Player player;
        private Assist assist;
        private String type;
        private String detail;
        private String comments;

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Time {
            private Long elapsed;
            private Long extra;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Team {
            private Long id;
            private String name;
            private String logo;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Player {
            private Long id;
            private String name;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Assist {
            private Long id;
            private String name;
        }
    }

    /**
     * API 측에서 홈팀, 어웨팀 해서 2개가 Array 로 들어옴
     */
    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Lineups {
        private Team team;
        private String formation;
        private List<StartXI> startXI;
        private List<Substitute> substitutes;
        private Coach coach;

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Team {
            private Long id;
            private String name;
            private String logo;
            private Colors colors;

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Colors {
                private Player player;
                private Goalkeeper goalkeeper;

                @Getter
                @Setter
                @ToString
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Player {
                    private String primary;
                    private String number;
                    private String border;
                }

                @Getter
                @Setter
                @ToString
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Goalkeeper {
                    private String primary;
                    private String number;
                    private String border;
                }
            }
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Coach {
            private Long id;
            private String name;
            private String photo;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StartXI {
            private Player player;

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Player {
                private Long id;
                private String name;
                private String number;
                private String pos; // ex. "G" "D" "M" "F"
                private String grid; // ex. "2:3"
            }
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Substitute {
            private Player player;

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Player {
                private Long id;
                private String name;
                private String number;
                private String pos;
                private String grid;
            }
        }
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Statistics {
        private Team team;
        private List<StatisticsData> statistics;

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Team {
            private Long id;
            private String name;
            private String logo;
        }

        // Api 에서는 "statistics"
        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StatisticsData {
            private String type;
            private String value;
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
    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FixturePlayers {
        private Team team;
        private List<PlayerStatistics> players;

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Team {
            private Long id;
            private String name;
            private String logo;
            private String update; // ex. "2024-06-22T04:01:16+00:00"
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PlayerStatistics {
            private Player player;
            private List<Statistics> statistics;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Player {
            private Long id;
            private String name;
            private String photo;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Statistics {
            private Games games;
            private Integer offsides;
            private Shots shots;
            private Goals goals;
            private Passes passes;
            private Tackles tackles;
            private Duels duels;
            private Dribbles dribbles;
            private Fouls fouls;
            private Cards cards;
            private Penalty penalty;

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Games {
                private Integer minutes;
                private Integer number;
                private String position;
                private String rating;
                private Boolean captain;
                private Boolean substitute;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Shots {
                private Integer total;
                private Integer on;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Goals {
                private Integer total;
                private Integer conceded;
                private Integer assists;
                private Integer saves;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Passes {
                private Integer total;
                private Integer key;
                private String accuracy;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Tackles {
                private Integer total;
                private Integer blocks;
                private Integer interceptions;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Duels {
                private Integer total;
                private Integer won;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Dribbles {
                private Integer attempts;
                private Integer success;
                private Integer past;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Fouls {
                private Integer drawn;
                private Integer committed;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Cards {
                private Integer yellow;
                private Integer red;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Penalty {
                private Integer won;
                private Integer committed;
                private Integer scored;
                private Integer missed;
                private Integer saved;
            }
        }
    }
}
