package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.*
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.core.entity.TeamCore

@Entity
@Table(
    name = "refac_apisports_match_team"
)
data class ApiSportsMatchTeam(
    @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_core_id")
    var teamCore: TeamCore? = null,

    @OneToMany(mappedBy = "matchTeam", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var players: MutableList<ApiSportsMatchPlayer> = mutableListOf(),

    @OneToMany(mappedBy = "matchTeam", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var teamStatistics: MutableList<ApiSportsMatchTeamStatistics> = mutableListOf(),
)
