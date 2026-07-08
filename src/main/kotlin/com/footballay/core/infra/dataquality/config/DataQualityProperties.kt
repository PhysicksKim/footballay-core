package com.footballay.core.infra.dataquality.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "footballay.data-quality")
data class DataQualityProperties(
    val enabled: Boolean = false,
    val rawCollection: RawCollection = RawCollection(),
    val duplicateGate: DuplicateGate = DuplicateGate(),
    val async: Async = Async(),
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

    data class Async(
        val corePoolSize: Int = 2,
        val maxPoolSize: Int = 4,
        val queueCapacity: Int = 1000,
    )

    data class Storage(
        val enabled: Boolean = false,
        val type: StorageType = StorageType.NOOP,
        val bucket: String = "",
        val region: String = "",
        val rawPrefix: String = "data-quality/raw",
        val localBaseDir: String = "build/data-quality/raw-storage",
        val localDownloadUrlTtl: Duration = Duration.ofMinutes(10),
    )

    data class Kafka(
        val enabled: Boolean = false,
        val producer: KafkaProducer = KafkaProducer(),
    )

    data class KafkaProducer(
        val enabled: Boolean = false,
        val rawCollectedTopic: String = "football-data-raw-collected",
    )
}

enum class StorageType {
    NOOP,
    LOCAL,
    S3,
}
