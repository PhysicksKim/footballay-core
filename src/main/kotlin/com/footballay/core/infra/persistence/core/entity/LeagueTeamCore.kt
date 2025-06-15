package com.footballay.core.infra.persistence.core.entity

import jakarta.persistence.*
import java.util.Objects

@Entity
@Table(
    name = "refac_league_team",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_league_team", columnNames = ["league_core_id", "team_core_id"])
    ]
)
class LeagueTeamCore (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_core_id", referencedColumnName = "id", nullable = false)
    var league: LeagueCore,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_core_id", referencedColumnName = "id", nullable = false)
    var team: TeamCore
) {
    override fun toString(): String {
        return "LeagueTeamCore(id=$id, league=${league?.id}, team=${team?.id})"
    }
}