package com.footballay.core.web.football.cache.hash

import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.dto.FixtureStatisticsResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultFixtureResponseCanonicalizerTest {
    private lateinit var canonicalizer: FixtureResponseCanonicalizer

    @BeforeEach
    fun setUp() {
        canonicalizer = DefaultFixtureResponseCanonicalizer()
    }

    @Test
    fun `canonicalize liveStatus - 정렬 변경 없이 그대로 유지`() {
        val response =
            FixtureLiveStatusResponse(
                fixtureUid = "fixture-1",
                liveStatus =
                    FixtureLiveStatusResponse.LiveStatus(
                        elapsed = 77,
                        shortStatus = "2H",
                        longStatus = "Second Half",
                        score = FixtureLiveStatusResponse.Score(home = 1, away = 2),
                    ),
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(canonical).isEqualTo(response)
    }

    @Test
    fun `canonicalize events - sequence 기준으로 정렬`() {
        val response =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 3, type = "Subst"),
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Card"),
                    ),
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(canonical.events.map { it.sequence }).containsExactly(1, 2, 3)
        assertThat(response.events.map { it.sequence }).containsExactly(3, 1, 2)
    }

    @Test
    fun `canonicalize events - 입력 순서가 달라도 동일한 canonical 결과`() {
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

        val firstCanonical = canonicalizer.canonicalize(first)
        val secondCanonical = canonicalizer.canonicalize(second)

        assertThat(firstCanonical).isEqualTo(secondCanonical)
    }

    @Test
    fun `canonicalize events - 한번 더 canonicalize 해도 결과가 유지된다`() {
        val response =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events =
                    listOf(
                        event(sequence = 3, type = "Subst"),
                        event(sequence = 1, type = "Goal"),
                        event(sequence = 2, type = "Card"),
                    ),
            )

        val canonical = canonicalizer.canonicalize(response)
        val canonicalAgain = canonicalizer.canonicalize(canonical)

        assertThat(canonicalAgain).isEqualTo(canonical)
    }

    @Test
    fun `canonicalize events - 중복 sequence 도 보조 키로 deterministic 하게 정렬된다`() {
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

        val firstCanonical = canonicalizer.canonicalize(first)
        val secondCanonical = canonicalizer.canonicalize(second)

        assertThat(firstCanonical.events.map { "${it.sequence}:${it.type}:${it.player?.matchPlayerUid}" })
            .containsExactly("1:Goal:mp-1", "2:Card:mp-first", "2:Subst:mp-second")
        assertThat(firstCanonical).isEqualTo(secondCanonical)
    }

    @Test
    fun `canonicalize events - 빈 events 도 그대로 유지된다`() {
        val response =
            FixtureEventsResponse(
                fixtureUid = "fixture-2",
                events = emptyList(),
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(canonical).isEqualTo(response)
    }

    @Test
    fun `canonicalize lineup - players 와 substitutes 를 matchPlayerUid 기준으로 정렬`() {
        val response =
            FixtureLineupResponse(
                fixtureUid = "fixture-3",
                lineup =
                    FixtureLineupResponse.Lineup(
                        home =
                            startLineup(
                                teamUid = "home-team",
                                players =
                                    listOf(
                                        lineupPlayer("mp-3"),
                                        lineupPlayer("mp-1"),
                                        lineupPlayer("mp-2"),
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
                                players =
                                    listOf(
                                        lineupPlayer("amp-2"),
                                        lineupPlayer("amp-1"),
                                    ),
                                substitutes =
                                    listOf(
                                        lineupPlayer("asub-3", substitute = true),
                                        lineupPlayer("asub-1", substitute = true),
                                        lineupPlayer("asub-2", substitute = true),
                                    ),
                            ),
                    ),
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(
            canonical.lineup.home!!
                .players
                .map { it.matchPlayerUid },
        ).containsExactly("mp-1", "mp-2", "mp-3")
        assertThat(
            canonical.lineup.home!!
                .substitutes
                .map { it.matchPlayerUid },
        ).containsExactly("sub-1", "sub-2")
        assertThat(
            canonical.lineup.away!!
                .players
                .map { it.matchPlayerUid },
        ).containsExactly("amp-1", "amp-2")
        assertThat(
            canonical.lineup.away!!
                .substitutes
                .map { it.matchPlayerUid },
        ).containsExactly("asub-1", "asub-2", "asub-3")

        assertThat(
            response.lineup.home!!
                .players
                .map { it.matchPlayerUid },
        ).containsExactly("mp-3", "mp-1", "mp-2")
    }

    @Test
    fun `canonicalize lineup - 한번 더 canonicalize 해도 결과가 유지된다`() {
        val response =
            FixtureLineupResponse(
                fixtureUid = "fixture-3",
                lineup =
                    FixtureLineupResponse.Lineup(
                        home =
                            startLineup(
                                teamUid = "home-team",
                                players = listOf(lineupPlayer("mp-3"), lineupPlayer("mp-1"), lineupPlayer("mp-2")),
                                substitutes = listOf(lineupPlayer("sub-2", substitute = true), lineupPlayer("sub-1", substitute = true)),
                            ),
                        away =
                            startLineup(
                                teamUid = "away-team",
                                players = listOf(lineupPlayer("amp-2"), lineupPlayer("amp-1")),
                                substitutes = listOf(lineupPlayer("asub-2", substitute = true), lineupPlayer("asub-1", substitute = true)),
                            ),
                    ),
            )

        val canonical = canonicalizer.canonicalize(response)
        val canonicalAgain = canonicalizer.canonicalize(canonical)

        assertThat(canonicalAgain).isEqualTo(canonical)
    }

    @Test
    fun `canonicalize lineup - 입력 순서가 달라도 동일한 canonical 결과`() {
        val first =
            FixtureLineupResponse(
                fixtureUid = "fixture-3",
                lineup =
                    FixtureLineupResponse.Lineup(
                        home =
                            startLineup(
                                teamUid = "home-team",
                                players = listOf(lineupPlayer("mp-3"), lineupPlayer("mp-1"), lineupPlayer("mp-2")),
                                substitutes = listOf(lineupPlayer("sub-2", substitute = true), lineupPlayer("sub-1", substitute = true)),
                            ),
                        away =
                            startLineup(
                                teamUid = "away-team",
                                players = listOf(lineupPlayer("amp-2"), lineupPlayer("amp-1")),
                                substitutes =
                                    listOf(
                                        lineupPlayer("asub-3", substitute = true),
                                        lineupPlayer("asub-1", substitute = true),
                                        lineupPlayer("asub-2", substitute = true),
                                    ),
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
                                players = listOf(lineupPlayer("mp-2"), lineupPlayer("mp-3"), lineupPlayer("mp-1")),
                                substitutes = listOf(lineupPlayer("sub-1", substitute = true), lineupPlayer("sub-2", substitute = true)),
                            ),
                        away =
                            startLineup(
                                teamUid = "away-team",
                                players = listOf(lineupPlayer("amp-1"), lineupPlayer("amp-2")),
                                substitutes =
                                    listOf(
                                        lineupPlayer("asub-2", substitute = true),
                                        lineupPlayer("asub-3", substitute = true),
                                        lineupPlayer("asub-1", substitute = true),
                                    ),
                            ),
                    ),
            )

        val firstCanonical = canonicalizer.canonicalize(first)
        val secondCanonical = canonicalizer.canonicalize(second)

        assertThat(firstCanonical).isEqualTo(secondCanonical)
    }

    @Test
    fun `canonicalize lineup - 중복 matchPlayerUid 도 보조 키로 deterministic 하게 정렬된다`() {
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
                                substitutes = emptyList(),
                            ),
                        away = null,
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
                                substitutes = emptyList(),
                            ),
                        away = null,
                    ),
            )

        val firstCanonical = canonicalizer.canonicalize(first)
        val secondCanonical = canonicalizer.canonicalize(second)

        assertThat(
            firstCanonical.lineup.home!!
                .players
                .map { "${it.matchPlayerUid}:${it.name}" },
        ).containsExactly("mp-1:Alpha", "mp-2:Second duplicate", "mp-2:Third duplicate")
        assertThat(firstCanonical).isEqualTo(secondCanonical)
    }

    @Test
    fun `canonicalize lineup - home 과 away 가 null 이어도 그대로 유지`() {
        val response =
            FixtureLineupResponse(
                fixtureUid = "fixture-3",
                lineup = FixtureLineupResponse.Lineup(home = null, away = null),
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(canonical).isEqualTo(response)
    }

    @Test
    fun `canonicalize lineup - 빈 players 와 substitutes 도 그대로 유지된다`() {
        val response =
            FixtureLineupResponse(
                fixtureUid = "fixture-3",
                lineup =
                    FixtureLineupResponse.Lineup(
                        home = startLineup(teamUid = "home-team", players = emptyList(), substitutes = emptyList()),
                        away = startLineup(teamUid = "away-team", players = emptyList(), substitutes = emptyList()),
                    ),
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(canonical).isEqualTo(response)
    }

    @Test
    fun `canonicalize statistics - playerStatistics 와 xg 를 정렬`() {
        val response =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home =
                    teamWithStatistics(
                        teamUid = "home-team",
                        playerUids = listOf("mp-3", "mp-1", "mp-2"),
                        xg = listOf(xg(15, "0.45"), xg(10, "0.20"), xg(15, "0.15")),
                    ),
                away =
                    teamWithStatistics(
                        teamUid = "away-team",
                        playerUids = listOf("amp-2", "amp-1"),
                        xg = listOf(xg(50, "0.80"), xg(50, "0.20"), xg(30, "0.50")),
                    ),
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(canonical.home!!.playerStatistics.map { it.player.matchPlayerUid }).containsExactly("mp-1", "mp-2", "mp-3")
        assertThat(
            canonical.home!!
                .teamStatistics.xg
                .map { "${it.elapsed}:${it.xg}" },
        ).containsExactly("10:0.20", "15:0.15", "15:0.45")

        assertThat(canonical.away!!.playerStatistics.map { it.player.matchPlayerUid }).containsExactly("amp-1", "amp-2")
        assertThat(
            canonical.away!!
                .teamStatistics.xg
                .map { "${it.elapsed}:${it.xg}" },
        ).containsExactly("30:0.50", "50:0.20", "50:0.80")
        assertThat(response.home!!.playerStatistics.map { it.player.matchPlayerUid }).containsExactly("mp-3", "mp-1", "mp-2")
        assertThat(
            response.home!!
                .teamStatistics.xg
                .map { "${it.elapsed}:${it.xg}" },
        ).containsExactly("15:0.45", "10:0.20", "15:0.15")
    }

    @Test
    fun `canonicalize statistics - 한번 더 canonicalize 해도 결과가 유지된다`() {
        val response =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home =
                    teamWithStatistics(
                        teamUid = "home-team",
                        playerUids = listOf("mp-3", "mp-1", "mp-2"),
                        xg = listOf(xg(15, "0.45"), xg(10, "0.20"), xg(15, "0.15")),
                    ),
                away =
                    teamWithStatistics(
                        teamUid = "away-team",
                        playerUids = listOf("amp-2", "amp-1"),
                        xg = listOf(xg(50, "0.80"), xg(50, "0.20"), xg(30, "0.50")),
                    ),
            )

        val canonical = canonicalizer.canonicalize(response)
        val canonicalAgain = canonicalizer.canonicalize(canonical)

        assertThat(canonicalAgain).isEqualTo(canonical)
    }

    @Test
    fun `canonicalize statistics - 입력 순서가 달라도 동일한 canonical 결과`() {
        val first =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home =
                    teamWithStatistics(
                        teamUid = "home-team",
                        playerUids = listOf("mp-3", "mp-1", "mp-2"),
                        xg = listOf(xg(15, "0.45"), xg(10, "0.20"), xg(15, "0.15")),
                    ),
                away =
                    teamWithStatistics(
                        teamUid = "away-team",
                        playerUids = listOf("amp-2", "amp-1"),
                        xg = listOf(xg(50, "0.80"), xg(50, "0.20"), xg(30, "0.50")),
                    ),
            )
        val second =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home =
                    teamWithStatistics(
                        teamUid = "home-team",
                        playerUids = listOf("mp-2", "mp-3", "mp-1"),
                        xg = listOf(xg(15, "0.15"), xg(15, "0.45"), xg(10, "0.20")),
                    ),
                away =
                    teamWithStatistics(
                        teamUid = "away-team",
                        playerUids = listOf("amp-1", "amp-2"),
                        xg = listOf(xg(30, "0.50"), xg(50, "0.20"), xg(50, "0.80")),
                    ),
            )

        val firstCanonical = canonicalizer.canonicalize(first)
        val secondCanonical = canonicalizer.canonicalize(second)

        assertThat(firstCanonical).isEqualTo(secondCanonical)
    }

    @Test
    fun `canonicalize statistics - 중복 matchPlayerUid 도 보조 키로 deterministic 하게 정렬된다`() {
        val first =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home =
                    FixtureStatisticsResponse.TeamWithStatistics(
                        team =
                            FixtureStatisticsResponse.TeamInfo(
                                teamUid = "home-team",
                                name = "home-team",
                                shortName = null,
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
                                xg = emptyList(),
                            ),
                        playerStatistics =
                            listOf(
                                playerStatistic(matchPlayerUid = "mp-2", name = "Second duplicate"),
                                playerStatistic(matchPlayerUid = "mp-1", name = "Alpha"),
                                playerStatistic(matchPlayerUid = "mp-2", name = "First duplicate"),
                            ),
                    ),
                away = null,
            )
        val second =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home =
                    FixtureStatisticsResponse.TeamWithStatistics(
                        team =
                            FixtureStatisticsResponse.TeamInfo(
                                teamUid = "home-team",
                                name = "home-team",
                                shortName = null,
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
                                xg = emptyList(),
                            ),
                        playerStatistics =
                            listOf(
                                playerStatistic(matchPlayerUid = "mp-2", name = "First duplicate"),
                                playerStatistic(matchPlayerUid = "mp-2", name = "Second duplicate"),
                                playerStatistic(matchPlayerUid = "mp-1", name = "Alpha"),
                            ),
                    ),
                away = null,
            )

        val firstCanonical = canonicalizer.canonicalize(first)
        val secondCanonical = canonicalizer.canonicalize(second)

        assertThat(firstCanonical.home!!.playerStatistics.map { "${it.player.matchPlayerUid}:${it.player.name}" })
            .containsExactly("mp-1:Alpha", "mp-2:First duplicate", "mp-2:Second duplicate")
        assertThat(firstCanonical).isEqualTo(secondCanonical)
    }

    @Test
    fun `canonicalize statistics - xg 정렬 키가 완전히 같아도 항목 수와 결과를 유지한다`() {
        val response =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home =
                    teamWithStatistics(
                        teamUid = "home-team",
                        playerUids = emptyList(),
                        xg = listOf(xg(10, "0.20"), xg(5, "0.10"), xg(10, "0.20")),
                    ),
                away = null,
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(canonical.home!!.teamStatistics.xg)
            .containsExactly(xg(5, "0.10"), xg(10, "0.20"), xg(10, "0.20"))
        assertThat(canonical.home!!.teamStatistics.xg).hasSize(3)
    }

    @Test
    fun `canonicalize statistics - home 과 away 가 null 이어도 그대로 유지`() {
        val response =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home = null,
                away = null,
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(canonical).isEqualTo(response)
    }

    @Test
    fun `canonicalize statistics - 빈 playerStatistics 와 xg 도 그대로 유지된다`() {
        val response =
            FixtureStatisticsResponse(
                fixture = FixtureStatisticsResponse.FixtureBasic(uid = "fixture-4", elapsed = 80, status = "2H"),
                home = teamWithStatistics(teamUid = "home-team", playerUids = emptyList(), xg = emptyList()),
                away = teamWithStatistics(teamUid = "away-team", playerUids = emptyList(), xg = emptyList()),
            )

        val canonical = canonicalizer.canonicalize(response)

        assertThat(canonical).isEqualTo(response)
    }

    private fun event(
        sequence: Int,
        type: String,
    ) = FixtureEventsResponse.EventInfo(
        sequence = sequence,
        elapsed = sequence * 10,
        extraTime = null,
        team = FixtureEventsResponse.TeamInfo(teamUid = "team-$sequence", name = "Team $sequence", shortName = null, playerColor = null),
        player = FixtureEventsResponse.PlayerInfo(matchPlayerUid = "mp-$sequence", playerUid = "player-$sequence", name = "Player $sequence", shortName = null, number = sequence),
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
        teamShortName = null,
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
        shortName = null,
        number = null,
        photo = null,
        position = if (substitute) "SUB" else "M",
        grid = null,
        substitute = substitute,
    )

    private fun teamWithStatistics(
        teamUid: String,
        playerUids: List<String>,
        xg: List<FixtureStatisticsResponse.XG>,
    ) = FixtureStatisticsResponse.TeamWithStatistics(
        team =
            FixtureStatisticsResponse.TeamInfo(
                teamUid = teamUid,
                name = teamUid,
                shortName = null,
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
        playerStatistics =
            playerUids.map { matchPlayerUid ->
                FixtureStatisticsResponse.PlayerWithStatistics(
                    player =
                        FixtureStatisticsResponse.PlayerInfo(
                            matchPlayerUid = matchPlayerUid,
                            playerUid = "player-$matchPlayerUid",
                            name = "Player $matchPlayerUid",
                            shortName = null,
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
            },
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
                shortName = null,
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

    private fun event(
        sequence: Int,
        type: String,
        playerSuffix: String,
    ) = FixtureEventsResponse.EventInfo(
        sequence = sequence,
        elapsed = sequence * 10,
        extraTime = null,
        team = FixtureEventsResponse.TeamInfo(teamUid = "team-$sequence", name = "Team $sequence", shortName = null, playerColor = null),
        player = FixtureEventsResponse.PlayerInfo(matchPlayerUid = "mp-$playerSuffix", playerUid = "player-$playerSuffix", name = "Player $playerSuffix", shortName = null, number = null),
        assist = null,
        type = type,
        detail = "$type detail",
        comments = null,
    )
}
