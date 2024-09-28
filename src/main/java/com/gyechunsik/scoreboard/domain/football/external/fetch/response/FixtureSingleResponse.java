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

    private List<_FixtureSingle> response;

    @Getter
    @Setter
    @ToString
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
        private String season;
        private String round;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Teams {
        private _Home home;
        private _Away away;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Home {
        private Long id;
        private String name;
        private String logo;
        private String winner;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Away {
        private Long id;
        private String name;
        private String logo;
        private String winner;
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

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Events {
        private _Time time;
        private _Team team;
        private _Player player;
        private _Assist assist;
        private String type;
        private String detail;
        private String comments;

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Time {
            private Integer elapsed;
            private Integer extra;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Team {
            private Long id;
            private String name;
            private String logo;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Player {
            private Long id;
            private String name;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Assist {
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
    public static class _Lineups {
        private _Team team;
        private String formation;
        private List<_StartPlayer> startXI;
        private List<_StartPlayer> substitutes;
        private _Coach coach;

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Team {
            private Long id;
            private String name;
            private String logo;
            private _Colors colors;

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Colors {
                private _Player player;
                private _Goalkeeper goalkeeper;

                @Getter
                @Setter
                @ToString
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class _Player {
                    private String primary;
                    private String number;
                    private String border;
                }

                @Getter
                @Setter
                @ToString
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class _Goalkeeper {
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
        public static class _Coach {
            private Long id;
            private String name;
            private String photo;
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _StartXI {
            private _Player player;

        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _StartPlayer {
            private _Player player;

        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Player {
            private Long id;
            private String name;
            private Integer number;
            private String pos;
            private String grid;
        }
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Statistics {
        private _Team team;
        private List<_StatisticsData> statistics;

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Team {
            private Long id;
            private String name;
            private String logo;
        }

        // Api 에서는 "statistics"
        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _StatisticsData {
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
    public static class _FixturePlayers {
        private _Team team;
        private List<_PlayerStatistics> players;

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Team {
            private Long id;
            private String name;
            private String logo;
            private String update; // ex. "2024-06-22T04:01:16+00:00"
        }

        @Getter
        @Setter
        @ToString
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
        }

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class _Player {
            private Long id;
            private String name;
            private String photo;
        }

        @Getter
        @Setter
        @ToString
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

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Games {
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
            public static class _Shots {
                private Integer total;
                private Integer on;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Goals {
                private Integer total;
                private Integer conceded;
                private Integer assists;
                private Integer saves;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Passes {
                private Integer total;
                private Integer key;
                private String accuracy;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Tackles {
                private Integer total;
                private Integer blocks;
                private Integer interceptions;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Duels {
                private Integer total;
                private Integer won;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Dribbles {
                private Integer attempts;
                private Integer success;
                private Integer past;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Fouls {
                private Integer drawn;
                private Integer committed;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Cards {
                private Integer yellow;
                private Integer red;
            }

            @Getter
            @Setter
            @ToString
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class _Penalty {
                private Integer won;
                private Integer committed;
                private Integer scored;
                private Integer missed;
                private Integer saved;
            }
        }
    }
}
