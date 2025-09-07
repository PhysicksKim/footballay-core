package com.footballay.core.infra.persistence.apisports.entity

import com.footballay.core.infra.persistence.core.entity.PlayerCore
import jakarta.persistence.*

@Entity
@Table(
    name = "refac_player_apisports"
)
data class PlayerApiSports(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_core_id", referencedColumnName = "id")
    var playerCore: PlayerCore? = null, // PlayerCore와 연관 관계 설정

    var apiId: Long? = null, // API 응답의 player.id 불완전한 경우 null 존재 가능
    var name: String? = null, // API 응답의 player.name
    var firstname: String? = null, // API 응답의 player.firstname
    var lastname: String? = null, // API 응답의 player.lastname
    var age: Int? = null, // API 응답의 player.age
    var birthDate: String? = null, // API 응답의 player.birth.date
    var birthPlace: String? = null, // API 응답의 player.birth.place
    var birthCountry: String? = null,
    var nationality: String? = null,
    var height: String? = null, // API 응답의 player.height
    var weight: String? = null, // API 응답의 player.weight
    var number: Int? = null, // API 응답의 player.number
    var position: String? = null, // API 응답의 player.position
    var photo: String? = null, // API 응답의 player.photo

    var preventUpdate: Boolean = false
)