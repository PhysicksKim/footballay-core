package com.footballay.core.infra.persistence.apisports.entity

import com.footballay.core.infra.persistence.core.entity.LeagueCore
import jakarta.persistence.*

@Entity
@Table(
    name = "refac_league_apisports",
)
data class LeagueApiSports(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_core_id", referencedColumnName = "id")
    var leagueCore: LeagueCore? = null,
    @Column(nullable = false, unique = true)
    var apiId: Long,
    var name: String,
    var type: String? = null,
    var logo: String? = null,
    var countryName: String? = null,
    var countryCode: String? = null,
    var countryFlag: String? = null,
    var currentSeason: Int? = null,
    @OneToMany(
        mappedBy = "leagueApiSports",
        cascade = [],
        fetch = FetchType.LAZY,
    )
    var seasons: List<LeagueApiSportsSeason> = emptyList(),
)
