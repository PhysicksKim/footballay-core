package com.footballay.core.infra.apisports.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "refac_league_api_sports_meta"
)
data class LeagueApiSportsMeta(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var currentSeason: Int = -1,

    @OneToMany(
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        mappedBy = "leagueApiSportsMeta"
    )
    var season: List<LeagueApiSportsSeason> = emptyList(),

    @Embedded
    var coverage: LeagueApiSportsCoverage? = null,
)
