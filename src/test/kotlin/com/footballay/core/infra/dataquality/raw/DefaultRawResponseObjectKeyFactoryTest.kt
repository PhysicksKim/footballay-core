package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.config.DataQualityProperties
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.raw.model.RawResponseObjectKeyCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class DefaultRawResponseObjectKeyFactoryTest {
    @Test
    fun `creates expected API Sports object key`() {
        val factory = factory()

        val objectKey =
            factory.create(
                RawResponseObjectKeyCommand(
                    provider = FootballDataProvider.API_SPORTS,
                    endpointKey = "fixture_single",
                    apiId = "1208397",
                    collectedAt = Instant.parse("2026-07-02T08:00:00Z"),
                    canonicalHash = "sha256-base64url",
                ),
            )

        assertThat(objectKey)
            .isEqualTo(
                "data-quality/raw/api-sports/fixture_single/2026/07/02/1208397/20260702T080000Z_sha256-base64url.json.gz",
            )
    }

    @Test
    fun `trims raw prefix slashes`() {
        val factory = factory(rawPrefix = "/data-quality/raw/")

        val objectKey =
            factory.create(
                RawResponseObjectKeyCommand(
                    provider = FootballDataProvider.API_SPORTS,
                    endpointKey = "fixture_single",
                    apiId = "1208397",
                    collectedAt = Instant.parse("2026-07-02T08:00:00Z"),
                    canonicalHash = "hash",
                ),
            )

        assertThat(objectKey).startsWith("data-quality/raw/api-sports/")
    }

    @Test
    fun `object key does not contain bucket region or domain`() {
        val factory =
            factory(
                bucket = "footballay-data-quality",
                region = "ap-northeast-2",
            )

        val objectKey =
            factory.create(
                RawResponseObjectKeyCommand(
                    provider = FootballDataProvider.API_SPORTS,
                    endpointKey = "fixture_single",
                    apiId = "1208397",
                    collectedAt = Instant.parse("2026-07-02T08:00:00Z"),
                    canonicalHash = "hash",
                ),
            )

        assertThat(objectKey).doesNotContain("footballay-data-quality")
        assertThat(objectKey).doesNotContain("ap-northeast-2")
        assertThat(objectKey).doesNotContain("http")
    }

    @Test
    fun `rejects path separators in endpoint key`() {
        val factory = factory()

        assertThatThrownBy {
            factory.create(
                RawResponseObjectKeyCommand(
                    provider = FootballDataProvider.API_SPORTS,
                    endpointKey = "fixture/single",
                    apiId = "1208397",
                    collectedAt = Instant.parse("2026-07-02T08:00:00Z"),
                    canonicalHash = "hash",
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("endpointKey must not contain path separators")
    }

    @Test
    fun `rejects URL or query string characters in api id`() {
        val factory = factory()

        assertThatThrownBy {
            factory.create(
                RawResponseObjectKeyCommand(
                    provider = FootballDataProvider.API_SPORTS,
                    endpointKey = "fixture_single",
                    apiId = "1208397?apiKey=secret",
                    collectedAt = Instant.parse("2026-07-02T08:00:00Z"),
                    canonicalHash = "hash",
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("apiId must not contain URL or query string characters")
    }

    @Test
    fun `rejects blank raw prefix`() {
        assertThatThrownBy {
            factory(rawPrefix = " / ")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("rawPrefix must not be blank")
    }

    private fun factory(
        rawPrefix: String = "data-quality/raw",
        bucket: String = "",
        region: String = "",
    ): DefaultRawResponseObjectKeyFactory =
        DefaultRawResponseObjectKeyFactory(
            DataQualityProperties(
                storage =
                    DataQualityProperties.Storage(
                        bucket = bucket,
                        region = region,
                        rawPrefix = rawPrefix,
                    ),
            ),
        )
}
