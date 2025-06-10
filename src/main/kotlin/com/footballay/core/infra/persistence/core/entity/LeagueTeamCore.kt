package com.footballay.core.infra.persistence.core.entity

import jakarta.persistence.*

@Entity
@Table(name = "refac_league_team")
class LeagueTeamCore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", referencedColumnName = "id")
    var league: LeagueCore? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", referencedColumnName = "id")
    var team: TeamCore? = null
}