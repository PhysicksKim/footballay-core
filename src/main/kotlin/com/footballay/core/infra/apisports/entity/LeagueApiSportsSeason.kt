package com.footballay.core.infra.apisports.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "refac_league_api_sports_season"
)
data class LeagueApiSportsSeason(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var seasonYear: Int? = -1,
    var seasonStart: String? = null,
    var seasonEnd: String? = null,

    @ManyToOne
    var apiSportsMeta: LeagueApiSportsMeta? = null
)