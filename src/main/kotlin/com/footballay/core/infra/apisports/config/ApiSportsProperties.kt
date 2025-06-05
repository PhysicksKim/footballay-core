package com.footballay.core.infra.apisports.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "footballay.apisports")
class ApiSportsProperties(
    val url: String,
    val headers: ApiSportsHeaders
)

data class ApiSportsHeaders(
    val xRapidapiHost: String,
    val xRapidapiKey: String
)