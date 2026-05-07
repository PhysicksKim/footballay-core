package com.footballay.core.infra.client.apisports.response

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApiSportsTeamResponseTest {
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `teams response allows missing nullable venue fields`() {
        val json =
            """
            {
              "team": {
                "id": 1,
                "name": "K League Team",
                "code": null,
                "country": "South-Korea",
                "founded": null,
                "national": false,
                "logo": null
              },
              "venue": {
                "id": 10,
                "name": "K League Stadium",
                "city": null,
                "capacity": null,
                "surface": null,
                "image": null
              }
            }
            """.trimIndent()

        val response = objectMapper.readValue(json, ApiSportsTeam.OfLeague::class.java)

        assertThat(response.venue).isNotNull
        assertThat(response.venue!!.id).isEqualTo(10)
        assertThat(response.venue!!.address).isNull()
    }

    @Test
    fun `teams response allows null venue`() {
        val json =
            """
            {
              "team": {
                "id": 1,
                "name": "K League Team",
                "code": null,
                "country": "South-Korea",
                "founded": null,
                "national": false,
                "logo": null
              },
              "venue": null
            }
            """.trimIndent()

        val response = objectMapper.readValue(json, ApiSportsTeam.OfLeague::class.java)

        assertThat(response.venue).isNull()
    }
}
