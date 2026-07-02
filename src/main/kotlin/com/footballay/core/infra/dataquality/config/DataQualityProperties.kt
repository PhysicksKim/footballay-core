package com.footballay.core.infra.dataquality.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "footballay.data-quality")
data class DataQualityProperties(
    val enabled: Boolean = false,
    val rawCollection: RawCollection = RawCollection(),
    val duplicateGate: DuplicateGate = DuplicateGate(),
    val storage: Storage = Storage(),
    val kafka: Kafka = Kafka(),
) {
    data class RawCollection(
        val enabled: Boolean = false,
    )

    data class DuplicateGate(
        val enabled: Boolean = false,
        val ttl: Duration = Duration.ofDays(7),
    )

    data class Storage(
        val enabled: Boolean = false,
        val type: StorageType = StorageType.NOOP,
        val bucket: String = "",
        val region: String = "",
        val rawPrefix: String = "data-quality/raw",
    )

    data class Kafka(
        val enabled: Boolean = false,
        val producer: KafkaProducer = KafkaProducer(),
        val consumer: KafkaConsumer = KafkaConsumer(),
    )

    data class KafkaProducer(
        val enabled: Boolean = false,
    )

    data class KafkaConsumer(
        val enabled: Boolean = false,
    )
}

enum class StorageType {
    NOOP,
    LOCAL,
    S3,
}
