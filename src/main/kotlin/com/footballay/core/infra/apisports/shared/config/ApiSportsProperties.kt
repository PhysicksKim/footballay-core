package com.footballay.core.infra.apisports.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "footballay.apisports")
data class ApiSportsProperties(
    val url: String = "v3.football.api-sports.io",
    val scheme: String = "https",
    val headers: ApiSportsHeaders = ApiSportsHeaders(),
)

data class ApiSportsHeaders(
    val xRapidapiKeyName: String = "mock-rapid-key-name",
    val xRapidapiKeyValue: String = "mock-api-key",
)
