package com.footballay.core.infra.dataquality.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.config.JacksonConfig
import com.footballay.core.infra.dataquality.raw.KafkaRawResponsePublisher
import com.footballay.core.infra.dataquality.raw.NoopRawResponseStorage
import com.footballay.core.infra.dataquality.raw.RawResponseDownloadUrlGenerator
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import com.footballay.core.infra.dataquality.raw.S3CompatibleRawResponseStorage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

/**
 * 설정에 따라 [DataQualityStorageConfig] 가 특정 구현체 빈 등록 및 설정값 등을 올바르게 처리하는지 테스트합니다.
 */
class DataQualityStorageConfigTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityStorageConfig::class.java)

    @Test
    fun `does not register S3 storage when data quality is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=s3",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponseStorage::class.java)
            }
    }

    @Test
    fun `does not register S3 storage when storage is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.storage.enabled=false",
                "footballay.data-quality.storage.type=s3",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponseStorage::class.java)
            }
    }

    @Test
    fun `registers noop storage when type is noop`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=noop",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponseStorage::class.java)
                assertThat(context).hasSingleBean(RawResponseDownloadUrlGenerator::class.java)
                assertThat(context.getBean(RawResponseStorage::class.java)).isInstanceOf(NoopRawResponseStorage::class.java)
            }
    }

    @Test
    fun `registers s3 storage and data quality s3 client when enabled and type is s3`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=s3",
                "footballay.data-quality.storage.bucket=footballay-data-quality",
                "footballay.data-quality.storage.region=ap-northeast-2",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponseStorage::class.java)
                assertThat(context).hasSingleBean(RawResponseDownloadUrlGenerator::class.java)
                assertThat(context.getBean(RawResponseStorage::class.java)).isInstanceOf(S3CompatibleRawResponseStorage::class.java)
                assertThat(context.getBean(RawResponseStorage::class.java))
                    .isSameAs(context.getBean(RawResponseDownloadUrlGenerator::class.java))
            }
    }

    @Test
    fun `S3 storage wins over noop storage when both configs are loaded`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityStorageConfig::class.java, DataQualityNoopConfig::class.java)
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=s3",
                "footballay.data-quality.storage.bucket=footballay-data-quality",
                "footballay.data-quality.storage.region=ap-northeast-2",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponseStorage::class.java)
                assertThat(context).hasSingleBean(RawResponseDownloadUrlGenerator::class.java)
                assertThat(context.getBean(RawResponseStorage::class.java)).isInstanceOf(S3CompatibleRawResponseStorage::class.java)
            }
    }

    @Test
    fun `registers S3 storage and Kafka publisher when all raw collection flags are enabled`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityStorageConfig::class.java, DataQualityKafkaProducerConfig::class.java)
            .withPropertyValues(
                "spring.kafka.bootstrap-servers=localhost:9092",
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.raw-collection.enabled=true",
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=s3",
                "footballay.data-quality.storage.bucket=footballay-data-quality",
                "footballay.data-quality.storage.region=ap-northeast-2",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
            ).run { context ->
                assertThat(context).hasSingleBean(S3CompatibleRawResponseStorage::class.java)
                assertThat(context).hasSingleBean(RawResponsePublisher::class.java)
                assertThat(context.getBean(RawResponsePublisher::class.java)).isInstanceOf(KafkaRawResponsePublisher::class.java)
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig {
        @Bean
        fun objectMapper(): ObjectMapper = JacksonConfig().objectMapper()
    }
}
