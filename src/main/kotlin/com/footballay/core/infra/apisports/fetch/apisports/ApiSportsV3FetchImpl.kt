package com.footballay.core.infra.apisports.fetch.apisports

import com.footballay.core.bodyObject
import com.footballay.core.domain.football.external.fetch.response.FixtureSingleResponse
import com.footballay.core.infra.apisports.config.ApiSportsProperties
import com.footballay.core.infra.apisports.fetch.response.ApiSportsV3Response
import com.footballay.core.infra.apisports.fetch.response.fixtures.ApiSportsFixtureSingle
import com.footballay.core.infra.apisports.fetch.response.leagues.ApiSportsLeaguesCurrent
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class ApiSportsV3FetchImpl (
    private val restClient: RestClient,
    private val properties: ApiSportsProperties
) : ApiSportsV3FetchService {

    private val log = logger()

    override fun fetchLeaguesCurrent(): ApiSportsV3Response<ApiSportsLeaguesCurrent> {
        val uri : URI = UriComponentsBuilder.newInstance()
            .scheme(properties.scheme)
            .host(properties.host)
            .path(properties.paths.leagues)
            .queryParam("current", true).build().toUri()
        log.info("Request leagues current from API Sports: $uri")

        return restClient.get()
            .uri(uri)
            .header(properties.headers.xRapidapiHostName,properties.headers.xRapidapiHostValue)
            .header(properties.headers.xRapidapiKeyName,properties.headers.xRapidapiKeyValue)
            .retrieve()
            .bodyObject<ApiSportsV3Response<ApiSportsLeaguesCurrent>>()
            ?: throw IllegalStateException("Response body is null of ApiSports League Current")
    }

    override fun fetchFixtureSingle(fixtureApiId: Long): ApiSportsV3Response<ApiSportsFixtureSingle> {
        val uri : URI = UriComponentsBuilder.newInstance()
            .scheme(properties.scheme)
            .host(properties.host)
            .path(properties.paths.fixtures)
            .queryParam("id", fixtureApiId).build().toUri()
        log.info("Request fixture single from API Sports: $uri")

        return restClient.get()
            .uri(uri)
            .header(properties.headers.xRapidapiHostName, properties.headers.xRapidapiHostValue)
            .header(properties.headers.xRapidapiKeyName, properties.headers.xRapidapiKeyValue)
            .retrieve()
            .bodyObject<ApiSportsV3Response<ApiSportsFixtureSingle>>()
            ?: throw IllegalStateException("Response body is null of ApiSports Fixture Single")
    }
}