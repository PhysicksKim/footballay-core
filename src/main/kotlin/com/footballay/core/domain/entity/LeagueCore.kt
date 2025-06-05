package com.footballay.core.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "refac_leagues")
data class LeagueCore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    /**
     * 외부 노출 ID
     */
    var uid: String? = null,

    var name: String,

    var available: Boolean = false,

)