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
    fun `binds default disabled values`() {
        contextRunner.run { context ->
            val properties = context.getBean(DataQualityProperties::class.java)

            assertThat(properties.enabled).isFalse()
            assertThat(properties.rawCollection.enabled).isFalse()
            assertThat(properties.duplicateGate.enabled).isFalse()
            assertThat(properties.duplicateGate.ttl).isEqualTo(Duration.ofDays(7))
            assertThat(properties.storage.enabled).isFalse()
            assertThat(properties.storage.type).isEqualTo(StorageType.NOOP)
            assertThat(properties.kafka.enabled).isFalse()
            assertThat(properties.kafka.producer.enabled).isFalse()
            assertThat(properties.kafka.consumer.enabled).isFalse()
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
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=s3",
                "footballay.data-quality.storage.bucket=footballay-data-quality",
                "footballay.data-quality.storage.region=ap-northeast-2",
                "footballay.data-quality.storage.raw-prefix=data-quality/raw",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
                "footballay.data-quality.kafka.consumer.enabled=true",
            ).run { context ->
                val properties = context.getBean(DataQualityProperties::class.java)

                assertThat(properties.enabled).isTrue()
                assertThat(properties.rawCollection.enabled).isTrue()
                assertThat(properties.duplicateGate.enabled).isTrue()
                assertThat(properties.duplicateGate.ttl).isEqualTo(Duration.ofDays(3))
                assertThat(properties.storage.enabled).isTrue()
                assertThat(properties.storage.type).isEqualTo(StorageType.S3)
                assertThat(properties.storage.bucket).isEqualTo("footballay-data-quality")
                assertThat(properties.storage.region).isEqualTo("ap-northeast-2")
                assertThat(properties.storage.rawPrefix).isEqualTo("data-quality/raw")
                assertThat(properties.kafka.enabled).isTrue()
                assertThat(properties.kafka.producer.enabled).isTrue()
                assertThat(properties.kafka.consumer.enabled).isTrue()
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig
}
