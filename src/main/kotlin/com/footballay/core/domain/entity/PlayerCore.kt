package com.footballay.core.domain.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "refac_players")
data class PlayerCore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    /**
     * 외부 노출 ID
     */
    var uid: String? = null,

    var name: String,

    var firstname: String? = null,

    var lastname: String? = null,

    var age: Int? = null,

    var birthDate: LocalDate? = null,

    var birthCountry: String? = null,

    var nationality: String? = null,

    var position: String? = null,

    var available: Boolean = false,

    /**
     * Core 엔티티에도 apiId를 두면, 동일 선수 중복여부 검사 시 편함
     */
    var apiId: Long? = null
)
