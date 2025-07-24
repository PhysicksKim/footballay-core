package com.footballay.core.infra.persistence.core.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "refac_team_player")
class TeamPlayerCore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_core_id", referencedColumnName = "id")
    var team: TeamCore? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_core_id", referencedColumnName = "id")
    var player: PlayerCore? = null
}
