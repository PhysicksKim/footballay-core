package com.footballay.core.infra.apisports.syncer.match.context

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam

/**
 * 매치 관련 JPA 엔티티들을 번들로 관리
 *
 * 영속성 엔티티들을 별도로 관리하여 Context와 분리
 * 저장 시 연관관계 설정에 활용
 */
data class MatchEntityBundle(
    var fixture: FixtureApiSports?,
    var homeTeam: ApiSportsMatchTeam?,
    var awayTeam: ApiSportsMatchTeam?,
    var allMatchPlayers: List<ApiSportsMatchPlayer>,
    var allEvents: List<ApiSportsMatchEvent>
) {
    companion object {
        fun createEmpty(): MatchEntityBundle {
            return MatchEntityBundle(
                fixture = null,
                homeTeam = null,
                awayTeam = null,
                allMatchPlayers = emptyList(),
                allEvents = emptyList()
            )
        }
    }
}

