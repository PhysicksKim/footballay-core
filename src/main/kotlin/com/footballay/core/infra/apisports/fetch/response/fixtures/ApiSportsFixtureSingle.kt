package com.footballay.core.infra.apisports.fetch.response.fixtures

import java.time.OffsetDateTime

data class ApiSportsFixtureSingle(
    val fixture: FixtureInfo,
    val league: LeagueInfo,
    val teams: TeamsInfo,
    val goals: GoalsInfo,
    val score: ScoreInfo,
    val events: List<EventInfo>,
    val lineups: List<LineupInfo>,
    val statistics: List<TeamStatisticsInfo>,
    val players: List<PlayerStatisticsInfo>
)

data class FixtureInfo(
    val id: Long,
    val referee: String?,
    val timezone: String,
    val date: OffsetDateTime,
    val timestamp: Long,
    val periods: Periods,
    val venue: Venue,
    val status: Status
)

data class Periods(
    val first: Long,
    val second: Long
)

data class Venue(
    val id: Long,
    val name: String,
    val city: String
)

data class Status(
    val long: String,
    val short: String,
    val elapsed: Int?,
    val extra: Int?
)

data class LeagueInfo(
    val id: Long,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String?,
    val season: Int,
    val round: String?,
    val standings: Boolean
)

data class TeamsInfo(
    val home: TeamDetail,
    val away: TeamDetail
)

data class TeamDetail(
    val id: Long,
    val name: String,
    val logo: String,
    val winner: Boolean?,
    val colors: TeamColors? = null // lineups only
)

data class TeamColors(
    val player: KitColors,
    val goalkeeper: KitColors
)

data class KitColors(
    val primary: String,
    val number: String,
    val border: String
)

data class GoalsInfo(
    val home: Int?,
    val away: Int?
)

data class ScoreInfo(
    val halftime: ScorePair,
    val fulltime: ScorePair,
    val extratime: ScorePair?,
    val penalty: ScorePair?
)

data class ScorePair(
    val home: Int?,
    val away: Int?
)

data class EventInfo(
    val time: EventTime,
    val team: TeamBrief,
    val player: PlayerBrief,
    val assist: PlayerBrief?,
    val type: String,
    val detail: String,
    val comments: String?
)

data class EventTime(
    val elapsed: Int,
    val extra: Int?
)

data class TeamBrief(
    val id: Long,
    val name: String,
    val logo: String
)

data class PlayerBrief(
    val id: Long?,
    val name: String?
)

data class LineupInfo(
    val team: TeamDetail,
    val coach: Coach,
    val formation: String,
    val startXI: List<StartPlayer>,
    val substitutes: List<StartPlayer>
)

data class Coach(
    val id: Long?,
    val name: String?,
    val photo: String?
)

data class StartPlayer(
    val player: LineupPlayer
)

data class LineupPlayer(
    val id: Long?,
    val name: String,
    val number: Int?,
    val pos: String,
    val grid: String?
)

data class TeamStatisticsInfo(
    val team: TeamBrief,
    val statistics: List<Statistic>
)

data class Statistic(
    val type: String,
    val value: Any?    // 숫자 or 퍼센트 문자열
)

data class PlayerStatisticsInfo(
    val team: TeamBrief,
    val players: List<PlayerStats>
)

data class PlayerStats(
    val player: PlayerDetail,
    val statistics: List<PlayerStatDetail>
)

data class PlayerDetail(
    val id: Long,
    val name: String,
    val photo: String?
)

data class PlayerStatDetail(
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
)

data class GameStats(
    val minutes: Int?,
    val number: Int?,
    val position: String?,
    val rating: String?,
    val captain: Boolean?,
    val substitute: Boolean?
)

data class ShotStats(
    val total: Int?,
    val on: Int?
)

data class GoalStats(
    val total: Int?,      // For out-field players
    val conceded: Int?,   // For goalkeepers
    val assists: Int?,
    val saves: Int?
)

data class PassStats(
    val total: Int?,
    val key: Int?,
    val accuracy: String?
)

data class TackleStats(
    val total: Int?,
    val blocks: Int?,
    val interceptions: Int?
)

data class DuelStats(
    val total: Int?,
    val won: Int?
)

data class DribbleStats(
    val attempts: Int?,
    val success: Int?,
    val past: Int?
)

data class FoulStats(
    val drawn: Int?,
    val committed: Int?
)

data class CardStats(
    val yellow: Int?,
    val red: Int?
)

data class PenaltyStats(
    val won: Int?,
    val commited: Int?,
    val scored: Int?,
    val missed: Int?,
    val saved: Int?
)