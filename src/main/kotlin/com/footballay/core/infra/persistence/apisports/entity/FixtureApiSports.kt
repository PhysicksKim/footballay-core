package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.*
import com.footballay.core.infra.persistence.core.entity.FixtureCore

@Entity
@Table(name = "refac_fixtures_api_sports")
data class FixtureApiSports(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_core_id", referencedColumnName = "id")
    var fixtureCore: FixtureCore? = null,

    var apiId: Long,
    var referee: String? = null,
    var timezone: String? = null,
    var date: String? = null,
    var timestamp: Long? = null,

    @ManyToOne
    var venue: VenueApiSports? = null, // API 응답의 fixture.venue

    @ManyToOne
    var status: FixtureStatusApiSports? = null, // API 응답의 fixture.status

    @ManyToOne
    var season: LeagueApiSportsSeason? = null // 시즌과 연관 관계 설정
)