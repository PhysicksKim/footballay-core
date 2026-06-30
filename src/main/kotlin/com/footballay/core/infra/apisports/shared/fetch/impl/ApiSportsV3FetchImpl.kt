package com.footballay.core.infra.apisports.shared.fetch.impl

import com.footballay.core.bodyObject
import com.footballay.core.infra.apisports.shared.config.ApiSportsProperties
import com.footballay.core.infra.apisports.shared.fetch.ApiSportsV3Fetcher
import com.footballay.core.infra.apisports.shared.fetch.response.*
import com.footballay.core.logger
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

/**
 * Implementation of [ApiSportsV3Fetcher] to fetch data from API Sports v3.
 *
 * Functions *TAKE SECONDS!* Don't use when the endpoint is called frequently.
 */
@Profile("!mockapi")
@Component
class ApiSportsV3FetchImpl(
    private val restClient: RestClient,
    private val properties: ApiSportsProperties,
) : ApiSportsV3Fetcher {
    private val log = logger()

    override fun fetchStatus(): ApiSportsV3LiveStatusEnvelope<ApiSportsAccountStatus> {
        log.info("ApiSports v3 fetch properties: $properties")
        log.info("ApiSports v3 fetch properties-key: ${properties.headers.xRapidapiKeyName} , ${properties.headers.xRapidapiKeyValue}")

        val uri: URI =
            ApiSportsUriBuilder()
                .path(ApiSportsPaths.status)
                .build()
                .toUri()
        logNameAndUri("status", uri)

        return apiSportsRestClientRequestBuild(uri)
            .bodyObject<ApiSportsV3LiveStatusEnvelope<ApiSportsAccountStatus>>()
            ?: throw IllegalStateException("Response body is null of ApiSports Status")
    }

    override fun fetchLeaguesCurrent(): ApiSportsV3Envelope<ApiSportsLeague.Current> {
        val uri: URI =
            ApiSportsUriBuilder()
                .path(ApiSportsPaths.leaguesCurrent)
                .queryParam("current", true)
                .build()
                .toUri()
        logNameAndUri("leagues current", uri)

        return apiSportsRestClientRequestBuild(uri)
            .bodyObject<ApiSportsV3Envelope<ApiSportsLeague.Current>>()
            ?: throw IllegalStateException("Response body is null of ApiSports League Current")
    }

    override fun fetchTeamsOfLeague(
        leagueApiId: Long,
        season: Int,
    ): ApiSportsV3Envelope<ApiSportsTeam.OfLeague> {
        val uri: URI =
            ApiSportsUriBuilder()
                .path(ApiSportsPaths.teamsOfLeague)
                .queryParam("league", leagueApiId)
                .queryParam("season", season)
                .build()
                .toUri()
        logNameAndUri("teams of league", uri)

        return apiSportsRestClientRequestBuild(uri)
            .bodyObject<ApiSportsV3Envelope<ApiSportsTeam.OfLeague>>()
            ?: throw IllegalStateException("Response body is null of ApiSports Teams of League")
    }

    override fun fetchSquadOfTeam(teamApiId: Long): ApiSportsV3Envelope<ApiSportsPlayer.OfTeam> {
        val uri: URI =
            ApiSportsUriBuilder()
                .path(ApiSportsPaths.squadOfTeam)
                .queryParam("team", teamApiId)
                .build()
                .toUri()
        logNameAndUri("squad of team", uri)

        return apiSportsRestClientRequestBuild(uri)
            .bodyObject<ApiSportsV3Envelope<ApiSportsPlayer.OfTeam>>()
            ?: throw IllegalStateException("Response body is null of ApiSports Squad of Team")
    }

    override fun fetchFixturesOfLeague(
        leagueApiId: Long,
        season: Int,
    ): ApiSportsV3Envelope<ApiSportsFixture.OfLeague> {
        val uri: URI =
            ApiSportsUriBuilder()
                .path(ApiSportsPaths.fixturesOfLeague)
                .queryParam("league", leagueApiId)
                .queryParam("season", season)
                .build()
                .toUri()
        logNameAndUri("fixtures of league", uri)

        return apiSportsRestClientRequestBuild(uri)
            .bodyObject<ApiSportsV3Envelope<ApiSportsFixture.OfLeague>>()
            ?: throw IllegalStateException("Response body is null of ApiSports Fixtures of League")
    }

    override fun fetchFixtureSingle(fixtureApiId: Long): ApiSportsV3Envelope<ApiSportsFixture.Single> {
        val uri: URI =
            ApiSportsUriBuilder()
                .path(ApiSportsPaths.fixtureSingle)
                .queryParam("id", fixtureApiId)
                .build()
                .toUri()
        logNameAndUri("fixture single", uri)

        return apiSportsRestClientRequestBuild(uri)
            .bodyObject<ApiSportsV3Envelope<ApiSportsFixture.Single>>()
            ?: throw IllegalStateException("Response body is null of ApiSports Fixture Single")
    }

    private fun apiSportsRestClientRequestBuild(uri: URI) =
        restClient
            .get()
            .uri(uri)
            .header(properties.headers.xRapidapiKeyName, properties.headers.xRapidapiKeyValue)
            .header("Accept", "application/json")
            .header("Accept-Encoding", "identity") // gzip 압축 방지
            .retrieve()

    private fun ApiSportsUriBuilder() =
        UriComponentsBuilder
            .newInstance()
            .scheme(properties.scheme)
            .host(properties.url)

    private fun logNameAndUri(
        reqName: String,
        uri: URI,
    ) {
        log.info("Request [$reqName] from API Sports: $uri")
    }
}
