package com.footballay.core.infra.dataquality.result

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.infra.dataquality.result.model.DataQualityResultMessage
import com.footballay.core.infra.persistence.dataquality.entity.DataQualityResultLog
import com.footballay.core.infra.persistence.dataquality.repository.DataQualityResultLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DataQualityResultIngestService(
    private val repository: DataQualityResultLogRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun ingest(message: DataQualityResultMessage): DataQualityResultLog? {
        require(message.eventId.isNotBlank()) { "eventId must not be blank" }
        require(message.sourceEventId.isNotBlank()) { "sourceEventId must not be blank" }
        require(message.endpointKey.isNotBlank()) { "endpointKey must not be blank" }
        require(message.apiId.isNotBlank()) { "apiId must not be blank" }
        require(message.canonicalHash.isNotBlank()) { "canonicalHash must not be blank" }
        require(message.rawJsonObjectKey.isNotBlank()) { "rawJsonObjectKey must not be blank" }
        require(message.scannerVersion.isNotBlank()) { "scannerVersion must not be blank" }
        require(message.summary.issueCount >= 0) { "issueCount must be non-negative" }

        if (repository.existsByResultEventId(message.eventId)) {
            return null
        }

        val entity =
            DataQualityResultLog(
                resultEventId = message.eventId,
                rawEventId = message.sourceEventId,
                provider = message.provider,
                endpointKey = message.endpointKey,
                apiId = message.apiId,
                canonicalHash = message.canonicalHash,
                rawJsonObjectKey = message.rawJsonObjectKey,
                scannerVersion = message.scannerVersion,
                checkedAt = message.checkedAt,
                issueCount = message.summary.issueCount,
                resultJson = objectMapper.writeValueAsString(message),
            )

        return repository.saveAndFlush(entity)
    }
}
