package com.footballay.core.matchdata.cache.hash

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureStatisticsResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class FixtureCanonicalJsonPipelineTest {
    private lateinit var canonicalizer: FixtureResponseCanonicalizer
    private lateinit var writer: FixtureCanonicalJsonWriter

    @BeforeEach
    fun setUp() {
        canonicalizer = DefaultFixtureResponseCanonicalizer()
        writer =
            DefaultFixtureCanonicalJsonWriter(
                JsonMapper
                    .builder()
                    .addModule(KotlinModule.Builder().build())
                    .addModule(JavaTimeModule())
                    .build(),
            )
    }

    @Test
    fun `events pipeline - 입력 순서가 달라도 동일한 compact json 으로 수렴한다`() {
        val first =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 3, type = "Subst"),
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Card"),
                    ),
            )
        val second =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 2, type = "Card"),
                        event(sequence = 3, type = "Subst"),
                        event(sequence = 1, type = "Goal"),
                    ),
            )

        val firstJson = canonicalJson(first)
        val secondJson = canonicalJson(second)

        assertThat(firstJson).isEqualTo(secondJson)
        assertThat(firstJson).doesNotContain("\n", "\r")
        assertThat(firstJson).isEqualTo(firstJson.trim())
    }

    @Test
    fun `events pipeline - 중복 sequence 도 동일한 canonical json 으로 수렴한다`() {
        val first =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 2, type = "Subst", playerSuffix = "second"),
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Card", playerSuffix = "first"),
                    ),
            )
        val second =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 2, type = "Card", playerSuffix = "first"),
                        event(sequence = 2, type = "Subst", playerSuffix = "second"),
                        event(sequence = 1, type = "Goal"),
                    ),
            )

        val firstJson = canonicalJson(first)
        val secondJson = canonicalJson(second)

        assertThat(firstJson).isEqualTo(secondJson)
        assertThat(firstJson).contains(""""sequence":1""", """"sequence":2""")
    }

    @Test
    fun `lineup pipeline - 입력 순서와 중복 uid 가 달라도 동일한 canonical bytes 로 수렴한다`() {
        val first =
            FixtureLineupResponse(
                fixtureUid = "fixture-3",
                lineup =
                    FixtureLineupResponse.Lineup(
                        home =
                            startLineup(
                                teamUid = "home-team",
                                players =
                                    listOf(
                                        lineupPlayer("mp-2", name = "Third duplicate"),
                                        lineupPlayer("mp-1", name = "Alpha"),
                                        lineupPlayer("mp-2", name = "Second duplicate"),
                                    ),
                                substitutes =
                                    listOf(
                                        lineupPlayer("sub-2", substitute = true),
                                        lineupPlayer("sub-1", substitute = true),
                                    ),
                            ),
                        away =
                            startLineup(
                                teamUid = "away-team",
                                players = listOf(lineupPlayer("amp-2"), lineupPlayer("amp-1")),
                                substitutes = emptyList(),
                            ),
                    ),
            )
        val second =
            FixtureLineupResponse(
                fixtureUid = "fixture-3",
                lineup =
                    FixtureLineupResponse.Lineup(
                        home =
                            startLineup(
                                teamUid = "home-team",
                                players =
                                    listOf(
                                        lineupPlayer("mp-2", name = "Second duplicate"),
                                        lineupPlayer("mp-2", name = "Third duplicate"),
                                        lineupPlayer("mp-1", name = "Alpha"),
                                    ),
                                substitutes =
                                    listOf(
                                        lineupPlayer("sub-1", substitute = true),
                                        lineupPlayer("sub-2", substitute = true),
                                    ),
                            ),
                        away =
                            startLineup(
                                teamUid = "away-team",
                                players = listOf(lineupPlayer("amp-1"), lineupPlayer("amp-2")),
                                substitutes = emptyList(),
                            ),
                    ),
            )

        val firstBytes = canonicalBytes(first)
        val secondBytes = canonicalBytes(second)

        assertThat(firstBytes).isEqualTo(secondBytes)
        assertThat(String(firstBytes, StandardCharsets.UTF_8)).doesNotContain("\n", "\r")
    }

    @Test
    fun `statistics pipeline - 입력 순서와 xg tie breaker 차이가 있어도 동일한 canonical json 으로 수렴한다`() {
        val first =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home =
                    teamWithStatistics(
                        teamUid = "home-team",
                        playerStatistics =
                            listOf(
                                playerStatistic(matchPlayerUid = "mp-2", name = "Second duplicate"),
                                playerStatistic(matchPlayerUid = "mp-1", name = "Alpha"),
                                playerStatistic(matchPlayerUid = "mp-2", name = "First duplicate"),
                            ),
                        xg = listOf(xg(15, "0.45"), xg(10, "0.20"), xg(15, "0.15")),
                    ),
                away =
                    teamWithStatistics(
                        teamUid = "away-team",
                        playerStatistics =
                            listOf(
                                playerStatistic(matchPlayerUid = "amp-2", name = "Away B"),
                                playerStatistic(matchPlayerUid = "amp-1", name = "Away A"),
                            ),
                        xg = listOf(xg(50, "0.80"), xg(50, "0.20"), xg(30, "0.50")),
                    ),
            )
        val second =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home =
                    teamWithStatistics(
                        teamUid = "home-team",
                        playerStatistics =
                            listOf(
                                playerStatistic(matchPlayerUid = "mp-2", name = "First duplicate"),
                                playerStatistic(matchPlayerUid = "mp-2", name = "Second duplicate"),
                                playerStatistic(matchPlayerUid = "mp-1", name = "Alpha"),
                            ),
                        xg = listOf(xg(15, "0.15"), xg(15, "0.45"), xg(10, "0.20")),
                    ),
                away =
                    teamWithStatistics(
                        teamUid = "away-team",
                        playerStatistics =
                            listOf(
                                playerStatistic(matchPlayerUid = "amp-1", name = "Away A"),
                                playerStatistic(matchPlayerUid = "amp-2", name = "Away B"),
                            ),
                        xg = listOf(xg(30, "0.50"), xg(50, "0.20"), xg(50, "0.80")),
                    ),
            )

        val firstJson = canonicalJson(first)
        val secondJson = canonicalJson(second)

        assertThat(firstJson).isEqualTo(secondJson)
        assertThat(firstJson).contains(""""xg":[{"elapsed":10,"xg":"0.20"},{"elapsed":15,"xg":"0.15"},{"elapsed":15,"xg":"0.45"}]""")
    }

    @Test
    fun `pipeline - semantic 값이 달라지면 canonical json 도 달라진다`() {
        val first =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Card"),
                    ),
            )
        val second =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Subst"),
                    ),
            )

        val firstJson = canonicalJson(first)
        val secondJson = canonicalJson(second)

        assertThat(firstJson).isNotEqualTo(secondJson)
    }

    private fun canonicalJson(response: FixtureEventsResponse): String = writer.writeAsString(canonicalizer.canonicalize(response))

    private fun canonicalJson(response: FixtureLineupResponse): String = writer.writeAsString(canonicalizer.canonicalize(response))

    private fun canonicalJson(response: FixtureStatisticsResponse): String = writer.writeAsString(canonicalizer.canonicalize(response))

    private fun canonicalBytes(response: FixtureLineupResponse): ByteArray = writer.writeAsBytes(canonicalizer.canonicalize(response))

    private fun event(
        sequence: Int,
        type: String,
        playerSuffix: String = sequence.toString(),
    ) = FixtureEventsResponse.EventInfo(
        sequence = sequence,
        elapsed = sequence * 10,
        extraTime = null,
        team = FixtureEventsResponse.TeamInfo(teamUid = "team-$sequence", name = "Team $sequence", koreanName = null, playerColor = null),
        player = FixtureEventsResponse.PlayerInfo(matchPlayerUid = "mp-$playerSuffix", playerUid = "player-$playerSuffix", name = "Player $playerSuffix", koreanName = null, number = sequence),
        assist = null,
        type = type,
        detail = "$type detail",
        comments = null,
    )

    private fun startLineup(
        teamUid: String,
        players: List<FixtureLineupResponse.LineupPlayer>,
        substitutes: List<FixtureLineupResponse.LineupPlayer>,
    ) = FixtureLineupResponse.StartLineup(
        teamUid = teamUid,
        teamName = teamUid,
        teamKoreanName = null,
        formation = "4-3-3",
        players = players,
        substitutes = substitutes,
        playerColor = null,
    )

    private fun lineupPlayer(
        matchPlayerUid: String,
        substitute: Boolean = false,
        name: String = "Player $matchPlayerUid",
    ) = FixtureLineupResponse.LineupPlayer(
        matchPlayerUid = matchPlayerUid,
        playerUid = "player-$matchPlayerUid",
        name = name,
        koreanName = null,
        number = null,
        photo = null,
        position = if (substitute) "SUB" else "M",
        grid = null,
        substitute = substitute,
    )

    private fun teamWithStatistics(
        teamUid: String,
        playerStatistics: List<FixtureStatisticsResponse.PlayerWithStatistics>,
        xg: List<FixtureStatisticsResponse.XG>,
    ) = FixtureStatisticsResponse.TeamWithStatistics(
        team =
            FixtureStatisticsResponse.TeamInfo(
                teamUid = teamUid,
                name = teamUid,
                koreanName = null,
                logo = null,
                playerColor = null,
            ),
        teamStatistics =
            FixtureStatisticsResponse.TeamStatistics(
                shotsOnGoal = 1,
                shotsOffGoal = 2,
                totalShots = 3,
                blockedShots = 4,
                shotsInsideBox = 5,
                shotsOutsideBox = 6,
                fouls = 7,
                cornerKicks = 8,
                offsides = 9,
                ballPossession = 10,
                yellowCards = 11,
                redCards = 12,
                goalkeeperSaves = 13,
                totalPasses = 14,
                passesAccurate = 15,
                passesAccuracyPercentage = 16,
                goalsPrevented = 17,
                xg = xg,
            ),
        playerStatistics = playerStatistics,
    )

    private fun playerStatistic(
        matchPlayerUid: String,
        name: String,
    ) = FixtureStatisticsResponse.PlayerWithStatistics(
        player =
            FixtureStatisticsResponse.PlayerInfo(
                matchPlayerUid = matchPlayerUid,
                playerUid = "player-$matchPlayerUid",
                name = name,
                koreanName = null,
                photo = null,
                position = "M",
                number = null,
            ),
        statistics =
            FixtureStatisticsResponse.PlayerStatistics(
                minutesPlayed = 90,
                position = "M",
                rating = "7.0",
                captain = false,
                substitute = false,
                shotsTotal = 0,
                shotsOn = 0,
                goals = 0,
                goalsConceded = 0,
                assists = 0,
                saves = 0,
                passesTotal = 0,
                passesKey = 0,
                passesAccuracy = 0,
                tacklesTotal = 0,
                interceptions = 0,
                duelsTotal = 0,
                duelsWon = 0,
                dribblesAttempts = 0,
                dribblesSuccess = 0,
                foulsCommitted = 0,
                foulsDrawn = 0,
                yellowCards = 0,
                redCards = 0,
                penaltiesScored = 0,
                penaltiesMissed = 0,
                penaltiesSaved = 0,
            ),
    )

    private fun xg(
        elapsed: Int,
        value: String,
    ) = FixtureStatisticsResponse.XG(
        elapsed = elapsed,
        xg = value,
    )
}
