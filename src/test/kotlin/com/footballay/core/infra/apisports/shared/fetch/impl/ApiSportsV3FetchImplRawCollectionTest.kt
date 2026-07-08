package com.footballay.core.infra.apisports.shared.fetch.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.config.JacksonConfig
import com.footballay.core.infra.apisports.shared.config.ApiSportsProperties
import com.footballay.core.infra.dataquality.raw.ApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.DefaultApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.RawResponseCanonicalHasher
import com.footballay.core.infra.dataquality.raw.RawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.RawResponseGzipCodec
import com.footballay.core.infra.dataquality.raw.RawResponseObjectKeyFactory
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectionCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseParameter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.RejectedExecutionException

class ApiSportsV3FetchImplRawCollectionTest {
    private val objectMapper: ObjectMapper = JacksonConfig().objectMapper()
    private val properties = ApiSportsProperties()

    @Test
    fun `fixture single fetch maps raw body to DTO and sends raw body to collector before returning`() {
        val collector = mock<ApiSportsRawResponseCollector>()
        val fixture = testFixture(collector = collector)
        fixture.expectGetFixtureSingle(FIXTURE_SINGLE_JSON)

        val response = fixture.fetcher.fetchFixtureSingle(1208397)

        assertThat(response.response).hasSize(1)
        assertThat(response.response.first().fixture.id).isEqualTo(1208397)
        assertThat(response.response.first().teams.home.name).isEqualTo("Manchester United")

        val commandCaptor = argumentCaptor<RawResponseCollectionCommand>()
        verify(collector).collect(commandCaptor.capture())
        assertThat(commandCaptor.firstValue.provider).isEqualTo(FootballDataProvider.API_SPORTS)
        assertThat(commandCaptor.firstValue.endpointKey).isEqualTo("fixtureSingle")
        assertThat(commandCaptor.firstValue.parameters)
            .containsExactly(RawResponseParameter("fixtureId", "1208397"))
        assertThat(commandCaptor.firstValue.rawJson).isEqualTo(FIXTURE_SINGLE_JSON)
        assertThat(commandCaptor.firstValue.collectedAt).isEqualTo(FIXED_NOW)

        fixture.server.verify()
    }

    @Test
    fun `non fixture single fetch maps DTO without raw collection`() {
        val collector = mock<ApiSportsRawResponseCollector>()
        val fixture = testFixture(collector = collector)
        fixture.expectGetLeaguesCurrent(LEAGUES_CURRENT_JSON)

        val response = fixture.fetcher.fetchLeaguesCurrent()

        assertThat(response.response).hasSize(1)
        assertThat(response.response.first().league.id).isEqualTo(39)
        verify(collector, never()).collect(any())
        fixture.server.verify()
    }

    @Test
    fun `collector exception is swallowed and DTO mapping still succeeds`() {
        val collector =
            object : ApiSportsRawResponseCollector {
                override fun collect(command: RawResponseCollectionCommand) {
                    throw IllegalStateException("collector failed")
                }
            }
        val fixture = testFixture(collector = collector)
        fixture.expectGetFixtureSingle(FIXTURE_SINGLE_JSON)

        assertThatCode {
            val response = fixture.fetcher.fetchFixtureSingle(1208397)
            assertThat(response.response.first().fixture.id).isEqualTo(1208397)
        }.doesNotThrowAnyException()

        fixture.server.verify()
    }

    @Test
    fun `fetch returns DTO even when collector async task has not completed`() {
        val executor = CapturingThreadPoolTaskExecutor()
        val canonicalHasher = mock<RawResponseCanonicalHasher>()
        val duplicateGate = mock<RawResponseDuplicateGate>()
        val objectKeyFactory = mock<RawResponseObjectKeyFactory>()
        val gzipCodec = mock<RawResponseGzipCodec>()
        val storage = mock<RawResponseStorage>()
        val publisher = mock<RawResponsePublisher>()
        val collector =
            DefaultApiSportsRawResponseCollector(
                taskExecutor = executor,
                canonicalHasher = canonicalHasher,
                duplicateGate = duplicateGate,
                objectKeyFactory = objectKeyFactory,
                gzipCodec = gzipCodec,
                storage = storage,
                publisher = publisher,
            )
        val fixture = testFixture(collector = collector)
        fixture.expectGetFixtureSingle(FIXTURE_SINGLE_JSON)

        val response = fixture.fetcher.fetchFixtureSingle(1208397)

        assertThat(response.response.first().fixture.id).isEqualTo(1208397)
        assertThat(executor.tasks).hasSize(1)
        verify(canonicalHasher, never()).hash(any())
        fixture.server.verify()
    }

    @Test
    fun `DTO mapping failure still fails fetch`() {
        val collector = mock<ApiSportsRawResponseCollector>()
        val fixture = testFixture(collector = collector)
        fixture.expectGetFixtureSingle("""{"response":["not-a-fixture"]}""")

        assertThatThrownBy {
            fixture.fetcher.fetchFixtureSingle(1208397)
        }.isInstanceOf(Exception::class.java)
    }

    private fun testFixture(collector: ApiSportsRawResponseCollector): TestFixture {
        val restClientBuilder =
            RestClient
                .builder()
                .messageConverters {
                    it.add(MappingJackson2HttpMessageConverter(objectMapper))
                }
        val server = MockRestServiceServer.bindTo(restClientBuilder).build()
        val fetcher =
            ApiSportsV3FetchImpl(
                restClient = restClientBuilder.build(),
                properties = properties,
                objectMapper = objectMapper,
                rawResponseCollector = collector,
                clock = FIXED_CLOCK,
            )

        return TestFixture(fetcher = fetcher, server = server)
    }

    private fun TestFixture.expectGetLeaguesCurrent(responseBody: String) {
        server
            .expect(requestTo("https://v3.football.api-sports.io/leagues?current=true"))
            .andExpect(method(org.springframework.http.HttpMethod.GET))
            .andExpect(header(properties.headers.xRapidapiKeyName, properties.headers.xRapidapiKeyValue))
            .andExpect(header("Accept", "application/json"))
            .andExpect(header("Accept-Encoding", "identity"))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON))
    }

    private fun TestFixture.expectGetFixtureSingle(responseBody: String) {
        server
            .expect(requestTo("https://v3.football.api-sports.io/fixtures?id=1208397"))
            .andExpect(method(org.springframework.http.HttpMethod.GET))
            .andExpect(header(properties.headers.xRapidapiKeyName, properties.headers.xRapidapiKeyValue))
            .andExpect(header("Accept", "application/json"))
            .andExpect(header("Accept-Encoding", "identity"))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON))
    }

    private data class TestFixture(
        val fetcher: ApiSportsV3FetchImpl,
        val server: MockRestServiceServer,
    )

    private class CapturingThreadPoolTaskExecutor : ThreadPoolTaskExecutor() {
        val tasks = mutableListOf<Runnable>()

        override fun execute(task: Runnable) {
            tasks.add(task)
        }
    }

    private companion object {
        private val FIXED_NOW: Instant = Instant.parse("2026-07-03T03:00:00Z")
        private val FIXED_CLOCK: Clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        private const val LEAGUES_CURRENT_JSON = """
            {
              "get": "leagues",
              "parameters": {"current": "true"},
              "errors": [],
              "results": 1,
              "paging": {"current": 1, "total": 1},
              "response": [
                {
                  "league": {"id": 39, "name": "Premier League", "type": "League", "logo": "logo"},
                  "country": {"name": "England", "code": "GB", "flag": "flag"},
                  "seasons": []
                }
              ]
            }
        """
        private const val FIXTURE_SINGLE_JSON = """
            {
              "get": "fixtures",
              "parameters": {"id": "1208397"},
              "errors": [],
              "results": 1,
              "paging": {"current": 1, "total": 1},
              "response": [
                {
                  "fixture": {
                    "id": 1208397,
                    "referee": null,
                    "timezone": "UTC",
                    "date": "2026-07-02T19:00:00+00:00",
                    "timestamp": 1783028400,
                    "periods": {"first": 1783028400, "second": 1783032000},
                    "venue": {"id": 1, "name": "Old Trafford", "city": "Manchester"},
                    "status": {"long": "Match Finished", "short": "FT", "elapsed": 90, "extra": null}
                  },
                  "league": {
                    "id": 39,
                    "name": "Premier League",
                    "country": "England",
                    "logo": "league-logo",
                    "flag": "flag",
                    "season": 2026,
                    "round": "Regular Season - 1",
                    "standings": true
                  },
                  "teams": {
                    "home": {"id": 33, "name": "Manchester United", "logo": "home-logo", "winner": true},
                    "away": {"id": 34, "name": "Newcastle", "logo": "away-logo", "winner": false}
                  },
                  "goals": {"home": 2, "away": 1},
                  "score": {
                    "halftime": {"home": 1, "away": 0},
                    "fulltime": {"home": 2, "away": 1},
                    "extratime": {"home": null, "away": null},
                    "penalty": {"home": null, "away": null}
                  },
                  "events": [],
                  "lineups": [],
                  "statistics": [],
                  "players": []
                }
              ]
            }
        """
    }
}
