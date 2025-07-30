package com.footballay.core.infra.apisports.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "footballay.apisports")
class ApiSportsProperties(
    val scheme: String = "https",
    val host: String = "v3.football.api-sports.io",
    val headers: ApiSportsHeaders = ApiSportsHeaders(),
)

data class ApiSportsHeaders(
    val xRapidapiHostName: String = "x-rapidapi-host",
    val xRapidapiHostValue: String = "v3.football.api-sports.io",
    val xRapidapiKeyName: String = "x-rapid",
    val xRapidapiKeyValue: String = "mock-api-key"
)
