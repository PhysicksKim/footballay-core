package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "refac_league_api_sports_seasons"
)
data class LeagueApiSportsSeason(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var seasonYear: Int? = -1,
    var seasonStart: String? = null,
    var seasonEnd: String? = null,

    @Embedded
    var coverage: LeagueApiSportsCoverage? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_api_sports_id", referencedColumnName = "id")
    var leagueApiSports: LeagueApiSports? = null

)