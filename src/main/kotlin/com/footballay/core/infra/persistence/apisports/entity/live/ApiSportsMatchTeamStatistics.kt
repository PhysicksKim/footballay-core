package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.*

@Entity
@Table(name = "refac_apisports_match_team_stats")
data class ApiSportsMatchTeamStatistics(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_team_id")
    var matchTeam: ApiSportsMatchTeam,
)
