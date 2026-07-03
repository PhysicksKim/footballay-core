package com.footballay.core.infra.dataquality.raw

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.config.JacksonConfig
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectedEvent
import com.footballay.core.infra.dataquality.raw.model.RawResponseRequestMetadata
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
        assertThat(keyCaptor.firstValue).isEqualTo("API_SPORTS:fixture_single:1208397")

        val payload = objectMapper.readTree(payloadCaptor.firstValue)
        assertThat(payload["schemaVersion"].asInt()).isEqualTo(1)
        assertThat(payload["eventId"].asText()).isEqualTo("event-id")
        assertThat(payload["provider"].asText()).isEqualTo("API_SPORTS")
        assertThat(payload["endpointKey"].asText()).isEqualTo("fixture_single")
        assertThat(payload["apiId"].asText()).isEqualTo("1208397")
        assertThat(payload["canonicalHash"].asText()).isEqualTo("hash")
        assertThat(payload["rawJsonObjectKey"].asText()).isEqualTo(RAW_JSON_OBJECT_KEY)
        assertThat(payload["collectedAt"].asText()).isEqualTo("2026-07-03T03:00:00Z")
        assertThat(payload["request"]["method"].asText()).isEqualTo("GET")
        assertThat(payload["request"]["path"].asText()).isEqualTo("/fixtures")
        assertThat(payload["request"]["query"]["id"].asText()).isEqualTo("1208397")
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
            "data-quality/raw/api-sports/fixture_single/2026/07/03/1208397/20260703T030000Z_hash.json.gz"
        private val EVENT =
            RawResponseCollectedEvent(
                eventId = "event-id",
                provider = FootballDataProvider.API_SPORTS,
                endpointKey = "fixture_single",
                apiId = "1208397",
                canonicalHash = "hash",
                rawJsonObjectKey = RAW_JSON_OBJECT_KEY,
                collectedAt = Instant.parse("2026-07-03T03:00:00Z"),
                request =
                    RawResponseRequestMetadata(
                        method = "GET",
                        path = "/fixtures",
                        query = mapOf("id" to "1208397"),
                    ),
            )
    }
}
