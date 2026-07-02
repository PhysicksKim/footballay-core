package com.footballay.core.infra.dataquality.raw

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DefaultRawResponseGzipCodecTest {
    private val codec = DefaultRawResponseGzipCodec()

    @Test
    fun `compress and decompress minimal JSON`() {
        val rawJson = """{"response":[]}"""

        val compressed = codec.compress(rawJson)
        val decompressed = codec.decompress(compressed)

        assertThat(compressed).isNotEmpty()
        assertThat(compressed.size).isGreaterThan(0)
        assertThat(decompressed).isEqualTo(rawJson)
    }

    @Test
    fun `compress and decompress empty string`() {
        val rawJson = ""

        val compressed = codec.compress(rawJson)
        val decompressed = codec.decompress(compressed)

        assertThat(compressed).isNotEmpty()
        assertThat(decompressed).isEqualTo(rawJson)
    }

    @Test
    fun `compress and decompress unicode text`() {
        val rawJson = """{"team":{"name":"대한민국","city":"서울"},"status":"Match Finished"}"""

        val compressed = codec.compress(rawJson)
        val decompressed = codec.decompress(compressed)

        assertThat(decompressed).isEqualTo(rawJson)
    }

    @Test
    fun `compress and decompress large raw JSON`() {
        val rawJson =
            buildString {
                append("""{"response":[""")
                repeat(500) { index ->
                    if (index > 0) append(",")
                    append(
                        """
                        {
                          "fixture": {
                            "id": $index,
                            "status": {
                              "long": "Match Finished",
                              "short": "FT",
                              "elapsed": 90,
                              "extra": null
                            }
                          },
                          "events": [
                            {"time":{"elapsed":12,"extra":null},"type":"Goal","detail":"Normal Goal"},
                            {"time":{"elapsed":80,"extra":2},"type":"Card","detail":"Yellow Card"}
                          ]
                        }
                        """.trimIndent(),
                    )
                }
                append("""]}""")
            }

        val compressed = codec.compress(rawJson)
        val decompressed = codec.decompress(compressed)

        assertThat(rawJson).isNotEmpty()
        assertThat(compressed.size).isLessThan(rawJson.toByteArray(Charsets.UTF_8).size)
        assertThat(decompressed).isEqualTo(rawJson)
    }

    @Test
    fun `decompress rejects non gzip bytes`() {
        assertThatThrownBy {
            codec.decompress("not-gzip".toByteArray(Charsets.UTF_8))
        }.isInstanceOf(Exception::class.java)
    }
}
