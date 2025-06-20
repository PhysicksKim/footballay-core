package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.Embeddable

@Embeddable
class ApiSportsStatus {
    var longStatus: String? = null
    var shortStatus: String? = null
    var elapsed: Int? = null
    var extra: Int? = null
}