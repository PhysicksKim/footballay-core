package com.footballay.core.infra.apisports.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "footballay.apisports")
data class ApiSportsProperties(
    val url: String = "https://v3.football.api-sports.io",
    val scheme: String = "http",
    val headers: ApiSportsHeaders = ApiSportsHeaders(),
)

data class ApiSportsHeaders(
    val xRapidapiKeyName: String = "mock-rapid-key-name", // key-name과 매칭됨
    val xRapidapiKeyValue: String = "mock-api-key", // x-rapidapi-key와 매칭됨
)
