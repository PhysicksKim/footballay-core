package com.footballay.core.infra.dataquality.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.config.JacksonConfig
import com.footballay.core.infra.dataquality.result.DataQualityResultIngestService
import com.footballay.core.infra.dataquality.result.KafkaDataQualityResultListener
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class DataQualityKafkaConsumerConfigTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityKafkaConsumerConfig::class.java)

    @Test
    fun `does not register listener when data quality is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.consumer.enabled=true",
            ).run { context ->
                assertThat(context).doesNotHaveBean(KafkaDataQualityResultListener::class.java)
            }
    }

    @Test
    fun `does not register listener when kafka is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=false",
                "footballay.data-quality.kafka.consumer.enabled=true",
            ).run { context ->
                assertThat(context).doesNotHaveBean(KafkaDataQualityResultListener::class.java)
            }
    }

    @Test
    fun `does not register listener when consumer is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.consumer.enabled=false",
            ).run { context ->
                assertThat(context).doesNotHaveBean(KafkaDataQualityResultListener::class.java)
            }
    }

    @Test
    fun `registers listener and consumer infrastructure when enabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.consumer.enabled=true",
                "spring.kafka.bootstrap-servers=localhost:9092",
            ).run { context ->
                assertThat(context).hasSingleBean(KafkaDataQualityResultListener::class.java)
                assertThat(context).hasBean(DataQualityKafkaConsumerConfig.DATA_QUALITY_KAFKA_CONSUMER_FACTORY_BEAN_NAME)
                assertThat(context).hasBean(DataQualityKafkaConsumerConfig.DATA_QUALITY_KAFKA_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
            }
    }

    @Test
    fun `registers listener with spring boot kafka auto configuration`() {
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration::class.java))
            .withUserConfiguration(TestConfig::class.java, DataQualityKafkaConsumerConfig::class.java)
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.consumer.enabled=true",
                "spring.kafka.bootstrap-servers=localhost:9092",
            ).run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).hasSingleBean(KafkaDataQualityResultListener::class.java)
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig {
        @Bean
        fun objectMapper(): ObjectMapper = JacksonConfig().objectMapper()

        @Bean
        fun dataQualityResultIngestService(): DataQualityResultIngestService = mock()
    }
}
