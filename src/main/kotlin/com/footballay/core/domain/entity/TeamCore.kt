package com.footballay.core.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "refac_teams")
data class TeamCore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    /**
     * 외부 노출 ID
     */
    var uid: String? = null,

    var name: String,

    var code: String? = null,

    var country: String? = null,

    var founded: Int? = null,

    var national: Boolean = false,

    var available: Boolean = false,

    /**
     * Core 엔티티에도 apiId를 두면, 동일 팀 중복여부 검사 시 편함
     */
    var apiId: Long? = null
)
