package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.*
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports

@Entity
@Table(
    name = "refac_apisports_match_event"
)
data class ApiSportsMatchEvent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_api_id")
    var fixtureApi: FixtureApiSports,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_team_id")
    var matchTeam: ApiSportsMatchTeam? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    var player: ApiSportsMatchPlayer? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assist_id")
    var assist: ApiSportsMatchPlayer? = null,
)
