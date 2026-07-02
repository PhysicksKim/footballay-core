package com.footballay.core.infra.dataquality.raw

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DefaultRawResponseCanonicalHasherTest {
    private val hasher = DefaultRawResponseCanonicalHasher(ObjectMapper())

    @Test
    fun `hash ignores object field order`() {
        val first =
            """
            {
              "response": [
                {
                  "fixture": { "id": 1208397, "status": { "short": "FT", "elapsed": 90 } },
                  "goals": { "home": 2, "away": 1 }
                }
              ],
              "results": 1
            }
            """.trimIndent()
        val second =
            """
            {
              "results": 1,
              "response": [
                {
                  "goals": { "away": 1, "home": 2 },
                  "fixture": { "status": { "elapsed": 90, "short": "FT" }, "id": 1208397 }
                }
              ]
            }
            """.trimIndent()

        assertThat(hasher.hash(first)).isEqualTo(hasher.hash(second))
    }

    @Test
    fun `hash ignores map key order in nested request parameters`() {
        val first =
            """
            {
              "parameters": {
                "id": "1208397",
                "timezone": "Asia/Seoul",
                "season": "2024"
              },
              "response": []
            }
            """.trimIndent()
        val second =
            """
            {
              "response": [],
              "parameters": {
                "season": "2024",
                "timezone": "Asia/Seoul",
                "id": "1208397"
              }
            }
            """.trimIndent()

        assertThat(hasher.hash(first)).isEqualTo(hasher.hash(second))
    }

    @Test
    fun `hash ignores object field order inside arrays without sorting array elements`() {
        val first =
            """
            {
              "events": [
                {
                  "time": { "elapsed": 12, "extra": null },
                  "type": "Goal",
                  "detail": "Normal Goal"
                },
                {
                  "time": { "elapsed": 80, "extra": 2 },
                  "type": "Card",
                  "detail": "Yellow Card"
                }
              ]
            }
            """.trimIndent()
        val second =
            """
            {
              "events": [
                {
                  "detail": "Normal Goal",
                  "type": "Goal",
                  "time": { "extra": null, "elapsed": 12 }
                },
                {
                  "detail": "Yellow Card",
                  "type": "Card",
                  "time": { "extra": 2, "elapsed": 80 }
                }
              ]
            }
            """.trimIndent()

        assertThat(hasher.hash(first)).isEqualTo(hasher.hash(second))
    }

    @Test
    fun `hash ignores JSON formatting whitespace`() {
        val pretty =
            """
            {
              "a": 1,
              "b": {
                "c": true
              }
            }
            """.trimIndent()
        val compact = """{"a":1,"b":{"c":true}}"""

        assertThat(hasher.hash(pretty)).isEqualTo(hasher.hash(compact))
    }

    @Test
    fun `hash changes when text value whitespace changes`() {
        val first = """{"fixture":{"status":{"long":"Match Finished"}}}"""
        val second = """{"fixture":{"status":{"long":"Match  Finished"}}}"""
        val third = """{"fixture":{"status":{"long":" Match Finished "}}}"""

        assertThat(hasher.hash(first)).isNotEqualTo(hasher.hash(second))
        assertThat(hasher.hash(first)).isNotEqualTo(hasher.hash(third))
    }

    @Test
    fun `hash treats escaped and unescaped equivalent text as same value`() {
        val first = """{"team":{"name":"Manchester United"}}"""
        val second = """{"team":{"name":"Manchester\u0020United"}}"""

        assertThat(hasher.hash(first)).isEqualTo(hasher.hash(second))
    }

    @Test
    fun `hash changes when null changes to empty string`() {
        val first = """{"fixture":{"referee":null}}"""
        val second = """{"fixture":{"referee":""}}"""

        assertThat(hasher.hash(first)).isNotEqualTo(hasher.hash(second))
    }

    @Test
    fun `hash changes when null field is missing`() {
        val first = """{"fixture":{"referee":null,"id":1208397}}"""
        val second = """{"fixture":{"id":1208397}}"""

        assertThat(hasher.hash(first)).isNotEqualTo(hasher.hash(second))
    }

    @Test
    fun `hash preserves array order`() {
        val first = """{"events":[{"id":1},{"id":2}]}"""
        val second = """{"events":[{"id":2},{"id":1}]}"""

        assertThat(hasher.hash(first)).isNotEqualTo(hasher.hash(second))
    }

    @Test
    fun `hash preserves scalar array order`() {
        val first = """{"errors":["first","second"]}"""
        val second = """{"errors":["second","first"]}"""

        assertThat(hasher.hash(first)).isNotEqualTo(hasher.hash(second))
    }

    @Test
    fun `hash preserves fixture single response array order`() {
        val first =
            """
            {
              "response": [
                { "fixture": { "id": 1208397 }, "goals": { "home": 2, "away": 1 } },
                { "fixture": { "id": 1208398 }, "goals": { "home": 0, "away": 0 } }
              ]
            }
            """.trimIndent()
        val second =
            """
            {
              "response": [
                { "goals": { "away": 0, "home": 0 }, "fixture": { "id": 1208398 } },
                { "goals": { "away": 1, "home": 2 }, "fixture": { "id": 1208397 } }
              ]
            }
            """.trimIndent()

        assertThat(hasher.hash(first)).isNotEqualTo(hasher.hash(second))
    }

    @Test
    fun `hash changes when value changes`() {
        val first = """{"score":{"home":2,"away":1}}"""
        val second = """{"score":{"home":3,"away":1}}"""

        assertThat(hasher.hash(first)).isNotEqualTo(hasher.hash(second))
    }

    @Test
    fun `invalid JSON throws clear exception`() {
        assertThatThrownBy {
            hasher.hash("""{"response": [}""")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Raw response body is not valid JSON")
    }
}
