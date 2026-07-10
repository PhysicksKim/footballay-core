package com.footballay.core.infra.dataquality.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import java.time.Duration

class DataQualityPropertiesTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TestConfig::class.java))

    @Test
    fun `binds safe and valid default values`() {
        contextRunner.run { context ->
            val properties = context.getBean(DataQualityProperties::class.java)

            assertThat(properties.enabled).isFalse()
            assertThat(properties.rawCollection.enabled).isFalse()
            assertThat(properties.duplicateGate.enabled).isFalse()
            assertThat(properties.storage.enabled).isFalse()
            assertThat(properties.storage.type).isEqualTo(StorageType.NOOP)
            assertThat(properties.kafka.enabled).isFalse()
            assertThat(properties.kafka.producer.enabled).isFalse()
            assertThat(properties.kafka.producer.rawCollectedTopic).isNotBlank()

            assertThat(properties.duplicateGate.ttl).isPositive()
            assertThat(properties.async.corePoolSize).isGreaterThan(0)
            assertThat(properties.async.maxPoolSize).isGreaterThanOrEqualTo(properties.async.corePoolSize)
            assertThat(properties.async.queueCapacity).isGreaterThanOrEqualTo(0)
            assertThat(properties.storage.rawPrefix).isNotBlank()
            assertThat(properties.storage.localBaseDir).isNotBlank()
            assertThat(properties.storage.localDownloadUrlTtl).isPositive()
            assertThat(properties.storage.preflight.enabled).isFalse()
            assertThat(properties.storage.preflight.keyPrefix).isNotBlank()
        }
    }

    @Test
    fun `binds enabled nested values`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.raw-collection.enabled=true",
                "footballay.data-quality.duplicate-gate.enabled=true",
                "footballay.data-quality.duplicate-gate.ttl=3d",
                "footballay.data-quality.async.core-pool-size=3",
                "footballay.data-quality.async.max-pool-size=6",
                "footballay.data-quality.async.queue-capacity=500",
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=s3",
                "footballay.data-quality.storage.bucket=footballay-data-quality",
                "footballay.data-quality.storage.region=ap-northeast-2",
                "footballay.data-quality.storage.raw-prefix=data-quality/raw",
                "footballay.data-quality.storage.local-base-dir=/tmp/footballay-data-quality",
                "footballay.data-quality.storage.local-download-url-ttl=5m",
                "footballay.data-quality.storage.s3-download-url-ttl=7m",
                "footballay.data-quality.storage.preflight.enabled=true",
                "footballay.data-quality.storage.preflight.key-prefix=data-quality/raw/_preflight/custom",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
                "footballay.data-quality.kafka.producer.raw-collected-topic=custom-raw-collected",
            ).run { context ->
                val properties = context.getBean(DataQualityProperties::class.java)

                assertThat(properties.enabled).isTrue()
                assertThat(properties.rawCollection.enabled).isTrue()
                assertThat(properties.duplicateGate.enabled).isTrue()
                assertThat(properties.duplicateGate.ttl).isEqualTo(Duration.ofDays(3))
                assertThat(properties.async.corePoolSize).isEqualTo(3)
                assertThat(properties.async.maxPoolSize).isEqualTo(6)
                assertThat(properties.async.queueCapacity).isEqualTo(500)
                assertThat(properties.storage.enabled).isTrue()
                assertThat(properties.storage.type).isEqualTo(StorageType.S3)
                assertThat(properties.storage.bucket).isEqualTo("footballay-data-quality")
                assertThat(properties.storage.region).isEqualTo("ap-northeast-2")
                assertThat(properties.storage.rawPrefix).isEqualTo("data-quality/raw")
                assertThat(properties.storage.localBaseDir).isEqualTo("/tmp/footballay-data-quality")
                assertThat(properties.storage.localDownloadUrlTtl).isEqualTo(Duration.ofMinutes(5))
                assertThat(properties.storage.s3DownloadUrlTtl).isEqualTo(Duration.ofMinutes(7))
                assertThat(properties.storage.preflight.enabled).isTrue()
                assertThat(properties.storage.preflight.keyPrefix).isEqualTo("data-quality/raw/_preflight/custom")
                assertThat(properties.kafka.enabled).isTrue()
                assertThat(properties.kafka.producer.enabled).isTrue()
                assertThat(properties.kafka.producer.rawCollectedTopic).isEqualTo("custom-raw-collected")
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig
}
