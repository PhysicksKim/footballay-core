package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "refac_league_apisports_seasons",
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
    @JoinColumn(name = "league_apisports_id", referencedColumnName = "id")
    var leagueApiSports: LeagueApiSports? = null,
) {
    override fun toString(): String = "LeagueApiSportsSeason(seasonEnd=$seasonEnd, seasonStart=$seasonStart, seasonYear=$seasonYear, id=$id)"
}
