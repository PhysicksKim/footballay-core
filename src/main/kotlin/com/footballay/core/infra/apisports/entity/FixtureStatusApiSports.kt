package com.footballay.core.infra.apisports.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "refac_fixture_status_api_sports"
)
data class FixtureStatusApiSports(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var longStatus: String? = null,
    var shortStatus: String? = null,
    var elapsed: Int? = null,
    var extra: Int? = null
)
