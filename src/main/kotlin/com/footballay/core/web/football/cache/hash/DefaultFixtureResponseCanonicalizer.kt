package com.footballay.core.web.football.cache.hash

import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.dto.FixtureStatisticsResponse
import org.springframework.stereotype.Component

/**
 * 캐시 구분용 해시 작성 이전에 배열 순서를 고정하는 기본 canonicalizer 구현입니다.
 *
 * 이 구현은 응답 의미를 바꾸지 않는 범위에서만 정렬을 수행하며,
 * JSON 직렬화 규약 자체는 다루지 않습니다.
 */
@Component
class DefaultFixtureResponseCanonicalizer : FixtureResponseCanonicalizer {
    override fun canonicalize(response: FixtureLiveStatusResponse): FixtureLiveStatusResponse = response

    override fun canonicalize(response: FixtureEventsResponse): FixtureEventsResponse =
        response.copy(
            events = response.events.sortedWith(eventInfoComparator),
        )

    override fun canonicalize(response: FixtureLineupResponse): FixtureLineupResponse =
        response.copy(
            lineup =
                response.lineup.copy(
                    home = response.lineup.home?.let(::canonicalizeStartLineup),
                    away = response.lineup.away?.let(::canonicalizeStartLineup),
                ),
        )

    override fun canonicalize(response: FixtureStatisticsResponse): FixtureStatisticsResponse =
        response.copy(
            home = response.home?.let(::canonicalizeTeamWithStatistics),
            away = response.away?.let(::canonicalizeTeamWithStatistics),
        )

    private fun canonicalizeStartLineup(lineup: FixtureLineupResponse.StartLineup): FixtureLineupResponse.StartLineup =
        lineup.copy(
            players = lineup.players.sortedWith(lineupPlayerComparator),
            substitutes = lineup.substitutes.sortedWith(lineupPlayerComparator),
        )

    private fun canonicalizeTeamWithStatistics(team: FixtureStatisticsResponse.TeamWithStatistics): FixtureStatisticsResponse.TeamWithStatistics =
        team.copy(
            teamStatistics =
                team.teamStatistics.copy(
                    xg =
                        team.teamStatistics.xg.sortedWith(
                            compareBy<FixtureStatisticsResponse.XG> { it.elapsed }
                                .thenBy { it.xg },
                        ),
                ),
            playerStatistics =
                team.playerStatistics.sortedWith(
                    playerWithStatisticsComparator,
                ),
        )

    private companion object {
        val eventInfoComparator: Comparator<FixtureEventsResponse.EventInfo> =
            compareBy(
                { it.sequence },
                { it.elapsed },
                { it.extraTime },
                { it.type },
                { it.detail },
                { it.comments },
                { it.team.teamUid },
                { it.team.name },
                { it.team.koreanName },
                { it.team.playerColor?.primary },
                { it.team.playerColor?.number },
                { it.team.playerColor?.border },
                { it.player?.matchPlayerUid },
                { it.player?.playerUid },
                { it.player?.name },
                { it.player?.koreanName },
                { it.player?.number },
                { it.assist?.matchPlayerUid },
                { it.assist?.playerUid },
                { it.assist?.name },
                { it.assist?.koreanName },
                { it.assist?.number },
            )

        val lineupPlayerComparator: Comparator<FixtureLineupResponse.LineupPlayer> =
            compareBy<FixtureLineupResponse.LineupPlayer>(
                { it.matchPlayerUid },
                { it.playerUid },
                { it.name },
                { it.koreanName },
                { it.number },
                { it.photo },
                { it.position },
                { it.grid },
                { it.substitute },
            )

        val playerWithStatisticsComparator: Comparator<FixtureStatisticsResponse.PlayerWithStatistics> =
            compareBy<FixtureStatisticsResponse.PlayerWithStatistics>(
                { it.player.matchPlayerUid },
                { it.player.playerUid },
                { it.player.name },
                { it.player.koreanName },
                { it.player.photo },
                { it.player.position },
                { it.player.number },
                { it.statistics.minutesPlayed },
                { it.statistics.position },
                { it.statistics.rating },
                { it.statistics.captain },
                { it.statistics.substitute },
                { it.statistics.shotsTotal },
                { it.statistics.shotsOn },
                { it.statistics.goals },
                { it.statistics.goalsConceded },
                { it.statistics.assists },
                { it.statistics.saves },
                { it.statistics.passesTotal },
                { it.statistics.passesKey },
                { it.statistics.passesAccuracy },
                { it.statistics.tacklesTotal },
                { it.statistics.interceptions },
                { it.statistics.duelsTotal },
                { it.statistics.duelsWon },
                { it.statistics.dribblesAttempts },
                { it.statistics.dribblesSuccess },
                { it.statistics.foulsCommitted },
                { it.statistics.foulsDrawn },
                { it.statistics.yellowCards },
                { it.statistics.redCards },
                { it.statistics.penaltiesScored },
                { it.statistics.penaltiesMissed },
                { it.statistics.penaltiesSaved },
            )
    }
}
