package com.footballay.core.domain.sync.service

import com.footballay.core.domain.sync.service.response.ApiSportsV3Response
import com.footballay.core.domain.sync.service.response.LeagueItem
import com.footballay.core.domain.sync.service.response.LeaguesCurrentResponseType
import com.footballay.core.infra.apisports.config.ApiSportsProperties
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ApiSportsRequestService(
    private val restClient: RestClient,
    private val apiSportsProperties: ApiSportsProperties
) {
    fun requestLeaguesCurrent(): ApiSportsV3Response<LeagueItem> {
        return restClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .host(apiSportsProperties.url)
                    .path("/leagues")
                    .queryParam("current", true)
                    .build()
            }
            .header("X-RapidAPI-Host", apiSportsProperties.headers.xRapidapiHost)
            .header("X-RapidAPI-Key", apiSportsProperties.headers.xRapidapiKey)
            .retrieve()
            .body(LeaguesCurrentResponseType)
            ?: throw IllegalStateException("Response body is null of ApiSports League Current")
    }
}