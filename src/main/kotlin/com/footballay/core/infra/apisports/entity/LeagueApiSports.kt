package com.footballay.core.infra.apisports.entity

import com.footballay.core.domain.entity.LeagueCore
import jakarta.persistence.*

@Entity
@Table(
    name = "refac_league_api_sports"
)
data class LeagueApiSports(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_core_id", referencedColumnName = "id")
    var leagueCore: LeagueCore? = null,

    var apiId: Long? = null,
    var name: String,
    var type: String? = null,
    var logo: String? = null,
    var countryName: String? = null,
    var countryCode: String? = null,
    var countryFlag: String? = null,

    )
