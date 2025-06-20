package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "refac_fixture_status_api_sports"
)
data class FixtureStatusApiSports(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

)
