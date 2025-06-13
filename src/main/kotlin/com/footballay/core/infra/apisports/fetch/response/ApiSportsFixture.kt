package com.footballay.core.infra.apisports.fetch.response

import java.time.OffsetDateTime

object ApiSportsFixture {

    data class Single(
        val fixture: Fixture,
        val league: League,
        val teams: Teams,
        val goals: Goals,
        val score: Score,
        val events: List<Event>,
        val lineups: List<Lineup>,
        val statistics: List<TeamStatistics>,
        val players: List<PlayerStatistics>
    ) : FixtureResponse {

        data class Fixture(
            val id: Long,
            val referee: String?,
            val timezone: String,
            val date: OffsetDateTime,
            val timestamp: Long,
            val periods: Periods,
            val venue: Venue,
            val status: Status
        ) {
            data class Periods(val first: Long, val second: Long)
            data class Status(val long: String, val short: String, val elapsed: Int?, val extra: Int?)
            data class Venue(val id: Long, val name: String, val city: String)
        }

        data class League(
            val id: Int,
            val name: String,
            val country: String,
            val logo: String,
            val flag: String?,
            val season: Int,
            val round: String,
            val standings: Boolean
        )

        data class Teams(
            val home: Team,
            val away: Team
        ) {
            data class Team(
                val id: Long,
                val name: String,
                val logo: String,
                val winner: Boolean?,
                val colors: Colors? = null
            ) {
                data class Colors(
                    val player: KitColors,
                    val goalkeeper: KitColors
                ) {
                    data class KitColors(
                        val primary: String,
                        val number: String,
                        val border: String
                    )
                }
            }
        }

        data class Goals(val home: Int?, val away: Int?)

        data class Score(
            val halftime: Pair,
            val fulltime: Pair,
            val extratime: Pair?,
            val penalty: Pair?
        ) {
            data class Pair(val home: Int?, val away: Int?)
        }

        data class Event(
            val time: Time,
            val team: Team,
            val player: Player,
            val assist: Player?,
            val type: String,
            val detail: String,
            val comments: String?
        ) {
            data class Time(val elapsed: Int, val extra: Int?)
            data class Team(val id: Long, val name: String, val logo: String)
            data class Player(val id: Long?, val name: String?)
        }

        data class Lineup(
            val team: Teams.Team,
            val coach: Coach,
            val formation: String,
            val startXI: List<StartPlayer>,
            val substitutes: List<StartPlayer>
        ) {
            data class Coach(val id: Long?, val name: String?, val photo: String?)
            data class StartPlayer(val player: Player) {
                data class Player(
                    val id: Long?,
                    val name: String,
                    val number: Int?,
                    val pos: String,
                    val grid: String?
                )
            }
        }

        data class TeamStatistics(
            val team: Event.Team,
            val statistics: List<Statistic>
        ) {
            data class Statistic(val type: String, val value: Any?)
        }

        data class PlayerStatistics(
            val team: Event.Team,
            val players: List<Player>
        ) {
            data class Player(
                val player: Detail,
                val statistics: List<StatDetail>
            ) {
                data class Detail(val id: Long, val name: String, val photo: String?)
                data class StatDetail(
                    val games: GameStats,
                    val offsides: Int?,
                    val shots: ShotStats,
                    val goals: GoalStats,
                    val passes: PassStats,
                    val tackles: TackleStats?,
                    val duels: DuelStats?,
                    val dribbles: DribbleStats?,
                    val fouls: FoulStats?,
                    val cards: CardStats?,
                    val penalty: PenaltyStats?
                ) {
                    data class GameStats(
                        val minutes: Int?, val number: Int?, val position: String?,
                        val rating: String?, val captain: Boolean?, val substitute: Boolean?
                    )
                    data class ShotStats(val total: Int?, val on: Int?)
                    data class GoalStats(val total: Int?, val conceded: Int?, val assists: Int?, val saves: Int?)
                    data class PassStats(val total: Int?, val key: Int?, val accuracy: String?)
                    data class TackleStats(val total: Int?, val blocks: Int?, val interceptions: Int?)
                    data class DuelStats(val total: Int?, val won: Int?)
                    data class DribbleStats(val attempts: Int?, val success: Int?, val past: Int?)
                    data class FoulStats(val drawn: Int?, val committed: Int?)
                    data class CardStats(val yellow: Int?, val red: Int?)
                    data class PenaltyStats(
                        val won: Int?, val commited: Int?, val scored: Int?,
                        val missed: Int?, val saved: Int?
                    )
                }
            }
        }
    }

    data class OfLeague(
        val fixture: Fixture,
        val league: League,
        val teams: Teams,
        val goals: Goals,
        val score: Score
    ) : FixtureResponse {

        data class Fixture(
            val id: Long,
            val referee: String?,
            val timezone: String,
            val date: OffsetDateTime,
            val timestamp: Long,
            val periods: Periods,
            val venue: Venue,
            val status: Status
        ) {
            data class Periods(val first: Long, val second: Long)
            data class Status(val long: String, val short: String, val elapsed: Int?, val extra: Int?)
            data class Venue(val id: Long, val name: String, val city: String)
        }

        data class League(
            val id: Int,
            val name: String,
            val country: String,
            val logo: String,
            val flag: String?,
            val season: Int,
            val round: String,
            val standings: Boolean
        )

        data class Teams(
            val home: Team,
            val away: Team
        ) {
            data class Team(
                val id: Int,
                val name: String,
                val logo: String,
                val winner: Boolean?
            )
        }

        data class Goals(val home: Int?, val away: Int?)

        data class Score(
            val halftime: Pair,
            val fulltime: Pair,
            val extratime: Pair?,
            val penalty: Pair?
        ) {
            data class Pair(val home: Int?, val away: Int?)
        }
    }
}
