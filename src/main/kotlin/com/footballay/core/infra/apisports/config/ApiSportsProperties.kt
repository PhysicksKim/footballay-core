package com.footballay.core.infra.apisports.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "footballay.apisports")
class ApiSportsProperties(
    val scheme: String,
    val host: String,
    val headers: ApiSportsHeaders,
    val paths: ApiSportsPaths
)

data class ApiSportsHeaders(
    val xRapidapiHostName: String,
    val xRapidapiHostValue: String,
    val xRapidapiKeyName: String,
    val xRapidapiKeyValue: String,
)

data class ApiSportsPaths(
    val fixtures: String,
    val leagues: String,
)