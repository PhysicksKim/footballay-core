package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.*

@Entity
@Table(name = "refac_apisports_match_player")
data class ApiSportsMatchPlayer(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_team_id")
    var matchTeam: ApiSportsMatchTeam,

    @OneToMany(mappedBy = "player", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var events: MutableList<ApiSportsMatchEvent> = mutableListOf(),

    @OneToMany(mappedBy = "assist", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var assistEvents: MutableList<ApiSportsMatchEvent> = mutableListOf(),

    @OneToMany(mappedBy = "matchPlayer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var statistics: MutableList<ApiSportsMatchPlayerStatistics> = mutableListOf(),
)
