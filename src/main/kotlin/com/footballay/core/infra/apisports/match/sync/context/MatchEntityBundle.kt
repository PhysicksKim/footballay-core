package com.footballay.core.infra.apisports.match.sync.context

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayerStatistics
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeamStatistics

/**
 * 매치 관련 JPA 엔티티들을 번들로 관리
 *
 * 영속성 엔티티들을 별도로 관리하여 Context와 분리
 * 저장 시 연관관계 설정에 활용
 *
 * **중요한 설계 원칙:**
 * - MatchPlayer와 PlayerStats는 1:1 관계이므로 MatchPlayer.statistics 필드로 통합 관리
 * - 별도의 allPlayerStats 맵은 제거하여 데이터 중복 방지
 * - 일관성과 단순성 보장
 */
data class MatchEntityBundle(
    var fixture: FixtureApiSports?,
    var homeTeam: ApiSportsMatchTeam?,
    var awayTeam: ApiSportsMatchTeam?,
    var allMatchPlayers: Map<String, ApiSportsMatchPlayer>,
    var allEvents: List<ApiSportsMatchEvent>,
    var homeTeamStat: ApiSportsMatchTeamStatistics? = null,
    var awayTeamStat: ApiSportsMatchTeamStatistics? = null,
) {
    companion object {
        fun createEmpty(): MatchEntityBundle =
            MatchEntityBundle(
                fixture = null,
                homeTeam = null,
                awayTeam = null,
                allMatchPlayers = emptyMap(),
                allEvents = emptyList(),
            )
    }

    /**
     * PlayerStats를 키로 조회합니다.
     *
     * **핵심 로직:**
     * - allMatchPlayers에서 해당 키의 MatchPlayer를 찾음
     * - MatchPlayer.statistics 필드에서 PlayerStats 반환
     *
     * @param playerKey MatchPlayer 키
     * @return 해당하는 PlayerStats 또는 null
     */
    fun getPlayerStats(playerKey: String): ApiSportsMatchPlayerStatistics? = allMatchPlayers[playerKey]?.statistics

    /**
     * 모든 PlayerStats를 맵으로 반환합니다.
     *
     * **핵심 로직:**
     * - allMatchPlayers에서 statistics가 있는 것만 필터링
     * - 키 기반으로 맵 구성
     *
     * @return PlayerStats 맵 (키: playerKey, 값: PlayerStats)
     */
    fun getAllPlayerStats(): Map<String, ApiSportsMatchPlayerStatistics> =
        allMatchPlayers
            .mapNotNull { (key, player) ->
                player.statistics?.let { stats -> key to stats }
            }.toMap()

    /**
     * PlayerStats를 설정합니다.
     *
     * **핵심 로직:**
     * - allMatchPlayers에서 해당 키의 MatchPlayer를 찾음
     * - MatchPlayer.statistics 필드에 PlayerStats 설정
     *
     * @param playerKey MatchPlayer 키
     * @param playerStats 설정할 PlayerStats
     */
    fun setPlayerStats(
        playerKey: String,
        playerStats: ApiSportsMatchPlayerStatistics,
    ) {
        allMatchPlayers[playerKey]?.let { player ->
            player.statistics = playerStats
        }
    }
}
