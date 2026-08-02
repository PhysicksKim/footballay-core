package com.footballay.core.infra.dataquality.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.config.JacksonConfig
import com.footballay.core.infra.dataquality.raw.KafkaRawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.util.function.Supplier

class DataQualityKafkaProducerConfigTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityKafkaProducerConfig::class.java)
            .withBean(KafkaTemplate::class.java, Supplier { mock<KafkaTemplate<String, String>>() })

    @Test
    fun `does not register kafka publisher when all data quality flags are false`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=false",
                "footballay.data-quality.raw-collection.enabled=false",
                "footballay.data-quality.storage.enabled=false",
                "footballay.data-quality.kafka.enabled=false",
                "footballay.data-quality.kafka.producer.enabled=false",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponsePublisher::class.java)
            }
    }

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

    @Test
    fun `includes Aiven SASL SSL properties in the data quality producer factory`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityKafkaProducerConfig::class.java)
            .withPropertyValues(
                "spring.kafka.bootstrap-servers=aiven.example.test:12345",
                "spring.kafka.properties.security.protocol=SASL_SSL",
                "spring.kafka.properties.sasl.mechanism=SCRAM-SHA-256",
                "spring.kafka.properties.ssl.truststore.location=/run/secrets/aiven-kafka-ca.pem",
                "spring.kafka.properties.ssl.truststore.type=PEM",
                "spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username=\"example-user\" password=\"example password!$#\";",
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.kafka.enabled=true",
                "footballay.data-quality.kafka.producer.enabled=true",
            ).run { context ->
                @Suppress("UNCHECKED_CAST")
                val producerFactory =
                    context.getBean(DataQualityKafkaProducerConfig.DATA_QUALITY_KAFKA_PRODUCER_FACTORY_BEAN_NAME, ProducerFactory::class.java)
                        as DefaultKafkaProducerFactory<String, String>

                assertThat(producerFactory.configurationProperties)
                    .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, listOf("aiven.example.test:12345"))
                    .containsEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
                    .containsEntry(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256")
                    .containsEntry(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/run/secrets/aiven-kafka-ca.pem")
                    .containsEntry(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM")
                    .containsEntry(
                        SaslConfigs.SASL_JAAS_CONFIG,
                        "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"example-user\" password=\"example password!$#\";",
                    )
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig {
        @Bean
        fun objectMapper(): ObjectMapper = JacksonConfig().objectMapper()
    }
}
