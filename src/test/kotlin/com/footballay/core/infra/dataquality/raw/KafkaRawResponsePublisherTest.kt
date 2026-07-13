package com.footballay.core.infra.dataquality.raw

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.config.JacksonConfig
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectedEvent
import com.footballay.core.infra.dataquality.raw.model.RawResponseParameter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.time.Instant
import java.util.concurrent.CompletableFuture

class KafkaRawResponsePublisherTest {
    private val objectMapper: ObjectMapper = JacksonConfig().objectMapper()
    private val kafkaTemplate = mock<KafkaTemplate<String, String>>()
    private val publisher =
        KafkaRawResponsePublisher(
            kafkaTemplate = kafkaTemplate,
            objectMapper = objectMapper,
            topic = TOPIC,
        )

    @Test
    fun `publishes raw collected event as json string with stable kafka key`() {
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<String>()))
            .thenReturn(CompletableFuture.completedFuture(mock<SendResult<String, String>>()))

        publisher.publish(EVENT)

        val topicCaptor = argumentCaptor<String>()
        val keyCaptor = argumentCaptor<String>()
        val payloadCaptor = argumentCaptor<String>()
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture())

        assertThat(topicCaptor.firstValue).isEqualTo(TOPIC)
        assertThat(keyCaptor.firstValue).isEqualTo("API_SPORTS:fixtureSingle:fixtureId=1208397")

        val payload = objectMapper.readTree(payloadCaptor.firstValue)
        assertThat(payload["rawEventId"].asText()).isEqualTo("01JZK8T9CJ4S9ZZ9G0E0D7YQ9M")
        assertThat(payload["provider"].asText()).isEqualTo("API_SPORTS")
        assertThat(payload["endpointKey"].asText()).isEqualTo("fixtureSingle")
        assertThat(payload["parameters"][0]["name"].asText()).isEqualTo("fixtureId")
        assertThat(payload["parameters"][0]["value"].asText()).isEqualTo("1208397")
        assertThat(payload["canonicalHash"].asText()).isEqualTo("hash")
        assertThat(payload["rawJsonObjectKey"].asText()).isEqualTo(RAW_JSON_OBJECT_KEY)
        assertThat(payload["rawJsonDownloadUrl"].asText()).isEqualTo(RAW_JSON_DOWNLOAD_URL)
        assertThat(payload["rawJsonDownloadUrlExpiresAt"].asText()).isEqualTo("2026-07-03T03:10:00Z")
        assertThat(payload["collectedAt"].asText()).isEqualTo("2026-07-03T03:00:00Z")
        assertThat(payload.has("schemaVersion")).isFalse()
        assertThat(payload.has("eventId")).isFalse()
        assertThat(payload.has("apiId")).isFalse()
        assertThat(payload.has("request")).isFalse()
        assertThat(payload.has("rawJson")).isFalse()
        assertThat(payload.toString()).doesNotContain("""{"response"""")
    }

    @Test
    fun `send exception is swallowed`() {
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<String>()))
            .thenThrow(IllegalStateException("kafka unavailable"))

        assertThatCode {
            publisher.publish(EVENT)
        }.doesNotThrowAnyException()
    }

    @Test
    fun `async send failure is swallowed`() {
        val failedFuture = CompletableFuture<SendResult<String, String>>()
        failedFuture.completeExceptionally(IllegalStateException("send failed"))
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any<String>()))
            .thenReturn(failedFuture)

        assertThatCode {
            publisher.publish(EVENT)
        }.doesNotThrowAnyException()
    }

    private companion object {
        private const val TOPIC = "football-data-raw-collected"
        private const val RAW_JSON_OBJECT_KEY =
            "data-quality/raw/api-sports/fixtureSingle/2026/07/03/fixtureId-1208397/20260703T030000Z_hash.json.gz"
        private const val RAW_JSON_DOWNLOAD_URL = "https://example.com/$RAW_JSON_OBJECT_KEY"
        private val EVENT =
            RawResponseCollectedEvent(
                rawEventId = "01JZK8T9CJ4S9ZZ9G0E0D7YQ9M",
                provider = FootballDataProvider.API_SPORTS,
                endpointKey = "fixtureSingle",
                parameters = listOf(RawResponseParameter(name = "fixtureId", value = "1208397")),
                canonicalHash = "hash",
                rawJsonObjectKey = RAW_JSON_OBJECT_KEY,
                rawJsonDownloadUrl = RAW_JSON_DOWNLOAD_URL,
                rawJsonDownloadUrlExpiresAt = Instant.parse("2026-07-03T03:10:00Z"),
                collectedAt = Instant.parse("2026-07-03T03:00:00Z"),
            )
    }
}
