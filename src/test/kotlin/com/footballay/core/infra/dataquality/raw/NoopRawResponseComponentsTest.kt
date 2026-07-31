package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectedEvent
import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectionCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckResult
import com.footballay.core.infra.dataquality.raw.model.RawResponseParameter
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class NoopRawResponseComponentsTest {
    @Test
    fun `noop collector returns without side effect`() {
        val collector = NoopApiSportsRawResponseCollector()

        collector.collect(collectionCommand())
    }

    @Test
    fun `noop duplicate gate treats response as new`() {
        val gate = NoopRawResponseDuplicateGate()

        val result =
            gate.checkAndStore(
                RawResponseDuplicateCheckCommand(
                    provider = FootballDataProvider.API_SPORTS,
                    endpointKey = "fixtureSingle",
                    parameters = PARAMETERS,
                    canonicalHash = "hash",
                ),
            )

        assertThat(result).isEqualTo(RawResponseDuplicateCheckResult.New)
    }

    @Test
    fun `noop storage returns object key without upload`() {
        val storage = NoopRawResponseStorage()

        val stored =
            storage.upload(
                RawResponseUploadCommand(
                    rawJsonObjectKey = "data-quality/raw/api-sports/fixture_single/object.json.gz",
                    gzipBytes = byteArrayOf(1, 2, 3),
                ),
            )
        val downloadUrl =
            storage.createDownloadUrl(
                RawResponseDownloadUrlCommand(stored.rawJsonObjectKey),
            )

        assertThat(stored.rawJsonObjectKey).isEqualTo("data-quality/raw/api-sports/fixture_single/object.json.gz")
        assertThat(downloadUrl.downloadUrl).isEmpty()
        assertThat(downloadUrl.expiresAt).isEqualTo(Instant.EPOCH)
    }

    @Test
    fun `noop publisher returns without side effect`() {
        val publisher = NoopRawResponsePublisher()
        val command = collectionCommand()

        publisher.publish(
            RawResponseCollectedEvent(
                rawEventId = "01JZK8T9CJ4S9ZZ9G0E0D7YQ9M",
                provider = command.provider,
                endpointKey = command.endpointKey,
                parameters = command.parameters,
                canonicalHash = "hash",
                rawJsonObjectKey = "data-quality/raw/api-sports/fixtureSingle/object.json.gz",
                collectedAt = command.collectedAt,
            ),
        )
    }

    private fun collectionCommand(): RawResponseCollectionCommand =
        RawResponseCollectionCommand(
            provider = FootballDataProvider.API_SPORTS,
            endpointKey = "fixtureSingle",
            parameters = PARAMETERS,
            rawJson = """{"response":[]}""",
            collectedAt = Instant.parse("2026-07-02T08:00:00Z"),
        )

    private companion object {
        private val PARAMETERS = listOf(RawResponseParameter(name = "fixtureId", value = "1208397"))
    }
}
