package com.footballay.core.infra.dataquality.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.config.JacksonConfig
import com.footballay.core.infra.dataquality.raw.KafkaRawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import java.util.function.Supplier

class DataQualityKafkaProducerConfigTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityKafkaProducerConfig::class.java)
            .withBean(KafkaTemplate::class.java, Supplier { mock<KafkaTemplate<String, String>>() })

    @Test
    fun `does not register kafka publisher when data quality is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponsePublisher::class.java)
            }
    }

    @Test
    fun `does not register kafka publisher when kafka is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=false",
                "footballay.data-quality.kafka.producer.enabled=true",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponsePublisher::class.java)
            }
    }

    @Test
    fun `does not register kafka publisher when producer is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=false",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponsePublisher::class.java)
            }
    }

    @Test
    fun `registers kafka publisher and producer infrastructure without external kafka template`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityKafkaProducerConfig::class.java)
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponsePublisher::class.java)
                assertThat(context).hasBean(DataQualityKafkaProducerConfig.DATA_QUALITY_KAFKA_PRODUCER_FACTORY_BEAN_NAME)
                assertThat(context).hasBean(DataQualityKafkaProducerConfig.DATA_QUALITY_KAFKA_TEMPLATE_BEAN_NAME)
                assertThat(context.getBean(RawResponsePublisher::class.java))
                    .isInstanceOf(KafkaRawResponsePublisher::class.java)
            }
    }

    @Test
    fun `registers kafka publisher when enabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponsePublisher::class.java)
                assertThat(context.getBean(RawResponsePublisher::class.java))
                    .isInstanceOf(KafkaRawResponsePublisher::class.java)
            }
    }

    @Test
    fun `kafka publisher wins over noop publisher when both configs are loaded`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityKafkaProducerConfig::class.java, DataQualityNoopConfig::class.java)
            .withBean(KafkaTemplate::class.java, Supplier { mock<KafkaTemplate<String, String>>() })
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponsePublisher::class.java)
                assertThat(context.getBean(RawResponsePublisher::class.java))
                    .isInstanceOf(KafkaRawResponsePublisher::class.java)
            }
    }

    @Test
    fun `kafka publisher remains primary when noop config is loaded first`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityNoopConfig::class.java, DataQualityKafkaProducerConfig::class.java)
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
            ).run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBean(RawResponsePublisher::class.java))
                    .isInstanceOf(KafkaRawResponsePublisher::class.java)
            }
    }

    @Test
    fun `registers kafka publisher with spring boot kafka auto configuration`() {
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration::class.java))
            .withUserConfiguration(TestConfig::class.java, DataQualityKafkaProducerConfig::class.java)
            .withPropertyValues(
                "spring.kafka.bootstrap-servers=localhost:9092",
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
            ).run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).hasSingleBean(RawResponsePublisher::class.java)
                assertThat(context.getBean(RawResponsePublisher::class.java))
                    .isInstanceOf(KafkaRawResponsePublisher::class.java)
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig {
        @Bean
        fun objectMapper(): ObjectMapper = JacksonConfig().objectMapper()
    }
}
