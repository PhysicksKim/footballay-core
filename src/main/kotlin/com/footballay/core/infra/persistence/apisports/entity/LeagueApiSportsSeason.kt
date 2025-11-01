package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "league_apisports_season",
)
data class LeagueApiSportsSeason(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var seasonYear: Int? = -1,
    var seasonStart: LocalDate? = null,
    var seasonEnd: LocalDate? = null,
    @Embedded
    var coverage: LeagueApiSportsCoverage? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_apisports_id", referencedColumnName = "id")
    var leagueApiSports: LeagueApiSports? = null,
) {
    override fun toString(): String = "LeagueApiSportsSeason(seasonEnd=$seasonEnd, seasonStart=$seasonStart, seasonYear=$seasonYear, id=$id)"
}
