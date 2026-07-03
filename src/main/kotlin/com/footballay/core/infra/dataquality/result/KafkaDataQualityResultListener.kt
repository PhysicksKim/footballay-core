package com.footballay.core.infra.dataquality.result

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.infra.dataquality.result.model.DataQualityResultMessage
import com.footballay.core.logger
import org.springframework.kafka.annotation.KafkaListener

class KafkaDataQualityResultListener(
    private val objectMapper: ObjectMapper,
    private val ingestService: DataQualityResultIngestService,
) {
    private val log = logger()

    @KafkaListener(
        topics = ["\${footballay.data-quality.kafka.consumer.quality-result-topic:football-data-quality-result}"],
        groupId = "\${footballay.data-quality.kafka.consumer.group-id:footballay-core-data-quality-result}",
        containerFactory = "dataQualityKafkaListenerContainerFactory",
    )
    fun onMessage(payload: String) {
        val message =
            try {
                objectMapper.readValue(payload, DataQualityResultMessage::class.java)
            } catch (ex: Exception) {
                log.warn("Invalid data quality result message. payload={}", payload, ex)
                return
            }

        try {
            ingestService.ingest(message)
        } catch (ex: IllegalArgumentException) {
            log.warn(
                "Rejected malformed data quality result message. eventId={}, sourceEventId={}",
                message.eventId,
                message.sourceEventId,
                ex,
            )
        }
    }
}
