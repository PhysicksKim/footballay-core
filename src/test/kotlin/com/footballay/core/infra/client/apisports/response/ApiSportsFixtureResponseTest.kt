package com.footballay.core.infra.client.apisports.response

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApiSportsFixtureResponseTest {
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun `fixtures response allows null venue`() {
        val json =
            """
            {
              "fixture": {
                "id": 1,
                "referee": null,
                "timezone": "UTC",
                "date": "2026-05-05T10:00:00+00:00",
                "timestamp": 1777975200,
                "periods": { "first": 1777975200, "second": 1777978800 },
                "venue": null,
                "status": { "long": "Not Started", "short": "NS", "elapsed": null, "extra": null }
              },
              "league": {
                "id": 292,
                "name": "K League 1",
                "country": "South-Korea",
                "logo": "",
                "flag": null,
                "season": 2026,
                "round": "Regular Season - 1",
                "standings": true
              },
              "teams": {
                "home": { "id": 1, "name": "Home", "logo": "", "winner": null },
                "away": { "id": 2, "name": "Away", "logo": "", "winner": null }
              },
              "goals": { "home": null, "away": null },
              "score": {
                "halftime": { "home": null, "away": null },
                "fulltime": { "home": null, "away": null },
                "extratime": null,
                "penalty": null
              }
            }
            """.trimIndent()

        val response = objectMapper.readValue(json, ApiSportsFixture.OfLeague::class.java)

        assertThat(response.fixture.venue).isNull()
    }

    @Test
    fun `fixtures response allows nullable venue fields`() {
        val json =
            """
            {
              "fixture": {
                "id": 1,
                "referee": null,
                "timezone": "UTC",
                "date": "2026-05-05T10:00:00+00:00",
                "timestamp": 1777975200,
                "periods": { "first": 1777975200, "second": 1777978800 },
                "venue": { "id": 0, "name": "Pohang Steel Yard", "city": "Pohang" },
                "status": { "long": "Not Started", "short": "NS", "elapsed": null, "extra": null }
              },
              "league": {
                "id": 292,
                "name": "K League 1",
                "country": "South-Korea",
                "logo": "",
                "flag": null,
                "season": 2026,
                "round": "Regular Season - 1",
                "standings": true
              },
              "teams": {
                "home": { "id": 1, "name": "Home", "logo": "", "winner": null },
                "away": { "id": 2, "name": "Away", "logo": "", "winner": null }
              },
              "goals": { "home": null, "away": null },
              "score": {
                "halftime": { "home": null, "away": null },
                "fulltime": { "home": null, "away": null },
                "extratime": null,
                "penalty": null
              }
            }
            """.trimIndent()

        val response = objectMapper.readValue(json, ApiSportsFixture.OfLeague::class.java)

        assertThat(response.fixture.venue).isNotNull
        assertThat(response.fixture.venue!!.id).isZero()
    }
}
