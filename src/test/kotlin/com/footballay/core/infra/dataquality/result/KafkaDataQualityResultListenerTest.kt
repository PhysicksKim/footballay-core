package com.footballay.core.infra.dataquality.result

import com.footballay.core.config.JacksonConfig
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.result.model.DataQualityIssueItem
import com.footballay.core.infra.dataquality.result.model.DataQualityResultMessage
import com.footballay.core.infra.dataquality.result.model.DataQualityResultSummary
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class KafkaDataQualityResultListenerTest {
    private val objectMapper = JacksonConfig().objectMapper()
    private val ingestService = mock<DataQualityResultIngestService>()
    private val listener =
        KafkaDataQualityResultListener(
            objectMapper = objectMapper,
            ingestService = ingestService,
        )

    @Test
    fun `parses valid message and delegates ingest`() {
        val payload = objectMapper.writeValueAsString(validMessage())

        listener.onMessage(payload)

        verify(ingestService).ingest(validMessage())
    }

    @Test
    fun `invalid json does not crash app`() {
        assertThatCode {
            listener.onMessage("{invalid-json")
        }.doesNotThrowAnyException()

        verify(ingestService, never()).ingest(any())
    }

    @Test
    fun `malformed result rejected by ingest service does not crash app`() {
        whenever(ingestService.ingest(any()))
            .thenThrow(IllegalArgumentException("eventId must not be blank"))

        assertThatCode {
            listener.onMessage(objectMapper.writeValueAsString(validMessage()))
        }.doesNotThrowAnyException()
    }

    private fun validMessage(): DataQualityResultMessage =
        DataQualityResultMessage(
            eventId = "result-event-1",
            sourceEventId = "raw-event-1",
            provider = FootballDataProvider.API_SPORTS,
            endpointKey = "fixture_single",
            apiId = "1208397",
            canonicalHash = "sha256-base64url",
            rawJsonObjectKey = "data-quality/raw/api-sports/fixture_single/2026/07/02/1208397/sample.json.gz",
            checkedAt = Instant.parse("2026-07-02T08:00:10Z"),
            scannerVersion = "rule-2026-07-02",
            summary = DataQualityResultSummary(issueCount = 1),
            items = listOf(
                DataQualityIssueItem(
                    code = "EVENT_NEGATIVE_ELAPSED_CARD",
                    severity = "ERROR",
                    classification = "CONFIRMED_ISSUE",
                    path = "$.response[0].events[12].time.elapsed",
                    message = "Card event elapsed minute is negative.",
                ),
            ),
        )
}
