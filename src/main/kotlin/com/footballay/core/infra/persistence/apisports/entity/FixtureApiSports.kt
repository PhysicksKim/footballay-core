package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.*
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import java.time.OffsetDateTime

@Entity
@Table(name = "refac_fixtures_api_sports")
data class FixtureApiSports(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_core_id", referencedColumnName = "id")
    var core: FixtureCore? = null,

    @Column(name = "api_id", unique = true, nullable = false)
    var apiId: Long,

    var referee: String? = null,
    var timezone: String? = null,
    var date: OffsetDateTime? = null,
    var timestamp: Long? = null,
    var round: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    var venue: VenueApiSports? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    var season: LeagueApiSportsSeason,

    @Embedded
    var status: ApiSportsStatus? = null,

    @Embedded
    var score: ApiSportsScore? = null,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    var homeTeam: ApiSportsMatchTeam? = null,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    var awayTeam: ApiSportsMatchTeam? = null,

    @OneToMany(mappedBy = "fixtureApi", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var events: MutableList<ApiSportsMatchEvent> = mutableListOf(),
)