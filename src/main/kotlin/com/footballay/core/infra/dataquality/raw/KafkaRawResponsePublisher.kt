package com.footballay.core.infra.dataquality.raw

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectedEvent
import com.footballay.core.logger
import org.springframework.kafka.core.KafkaTemplate

class KafkaRawResponsePublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val topic: String,
) : RawResponsePublisher {
    private val log = logger()

    init {
        require(topic.isNotBlank()) {
            "footballay.data-quality.kafka.producer.raw-collected-topic must not be blank"
        }
    }

    override fun publish(event: RawResponseCollectedEvent) {
        val key = createKey(event)
        val payload =
            try {
                objectMapper.writeValueAsString(event)
            } catch (ex: Exception) {
                log.warn(
                    "Failed to serialize data quality raw collected event. provider={}, endpointKey={}, apiId={}, eventId={}",
                    event.provider,
                    event.endpointKey,
                    event.apiId,
                    event.eventId,
                    ex,
                )
                return
            }

        try {
            kafkaTemplate
                .send(topic, key, payload)
                .whenComplete { _, ex ->
                    if (ex != null) {
                        log.warn(
                            "Failed to publish data quality raw collected event. topic={}, key={}, eventId={}",
                            topic,
                            key,
                            event.eventId,
                            ex,
                        )
                    }
                }
        } catch (ex: Exception) {
            log.warn(
                "Failed to submit data quality raw collected event to Kafka. topic={}, key={}, eventId={}",
                topic,
                key,
                event.eventId,
                ex,
            )
        }
    }

    private fun createKey(event: RawResponseCollectedEvent): String = "${event.provider.name}:${event.endpointKey}:${event.apiId}"
}
