package com.footballay.core.infra.dataquality.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.infra.dataquality.raw.KafkaRawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
@EnableConfigurationProperties(KafkaProperties::class)
@ConditionalOnProperty(
    prefix = "footballay.data-quality",
    name = ["enabled", "kafka.enabled", "kafka.producer.enabled"],
    havingValue = "true",
)
class DataQualityKafkaProducerConfig {
    @Bean(DATA_QUALITY_KAFKA_PRODUCER_FACTORY_BEAN_NAME)
    fun dataQualityKafkaProducerFactory(kafkaProperties: KafkaProperties): ProducerFactory<String, String> {
        val producerProperties = kafkaProperties.buildProducerProperties()
        producerProperties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producerProperties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        return DefaultKafkaProducerFactory(producerProperties)
    }

    @Bean(DATA_QUALITY_KAFKA_TEMPLATE_BEAN_NAME)
    fun dataQualityKafkaTemplate(
        @Qualifier(DATA_QUALITY_KAFKA_PRODUCER_FACTORY_BEAN_NAME)
        producerFactory: ProducerFactory<String, String>,
    ): KafkaTemplate<String, String> = KafkaTemplate(producerFactory)

    @Bean(DATA_QUALITY_KAFKA_RAW_RESPONSE_PUBLISHER_BEAN_NAME)
    @Primary
    fun rawResponsePublisher(
        @Qualifier(DATA_QUALITY_KAFKA_TEMPLATE_BEAN_NAME)
        kafkaTemplate: KafkaTemplate<String, String>,
        objectMapper: ObjectMapper,
        properties: DataQualityProperties,
    ): RawResponsePublisher =
        KafkaRawResponsePublisher(
            kafkaTemplate = kafkaTemplate,
            objectMapper = objectMapper,
            topic = properties.kafka.producer.rawCollectedTopic,
        )

    companion object {
        const val DATA_QUALITY_KAFKA_PRODUCER_FACTORY_BEAN_NAME = "dataQualityKafkaProducerFactory"
        const val DATA_QUALITY_KAFKA_TEMPLATE_BEAN_NAME = "dataQualityKafkaTemplate"
        const val DATA_QUALITY_KAFKA_RAW_RESPONSE_PUBLISHER_BEAN_NAME = "dataQualityKafkaRawResponsePublisher"
    }
}
