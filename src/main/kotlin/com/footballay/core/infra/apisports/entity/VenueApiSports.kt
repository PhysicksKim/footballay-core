package com.footballay.core.infra.apisports.entity

import jakarta.persistence.*

@Entity
@Table(name = "refac_venue_api_sports")
data class VenueApiSports(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // 데이터베이스에서 자동 생성되는 ID

    var apiId: Long? = null, // API 응답의 venue.id
    var name: String? = null, // API 응답의 venue.name
    var address: String? = null, // API 응답의 venue.address
    var city: String? = null, // API 응답의 venue.city
    var capacity: Int? = null, // API 응답의 venue.capacity
    var surface: String? = null, // API 응답의 venue.surface
    var image: String? = null // API 응답의 venue.image
)
