package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.*

@Entity
@Table(name = "refac_apisports_match_player_stats")
data class ApiSportsMatchPlayerStatistics(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_player_id")
    var matchPlayer: ApiSportsMatchPlayer,
)
