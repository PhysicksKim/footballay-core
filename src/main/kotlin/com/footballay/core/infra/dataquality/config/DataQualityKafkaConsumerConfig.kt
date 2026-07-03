package com.footballay.core.infra.dataquality.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.infra.dataquality.result.DataQualityResultIngestService
import com.footballay.core.infra.dataquality.result.KafkaDataQualityResultListener
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@EnableKafka
@Configuration
@EnableConfigurationProperties(KafkaProperties::class)
@ConditionalOnProperty(
    prefix = "footballay.data-quality",
    name = ["enabled", "kafka.enabled", "kafka.consumer.enabled"],
    havingValue = "true",
)
class DataQualityKafkaConsumerConfig {
    @Bean(DATA_QUALITY_KAFKA_CONSUMER_FACTORY_BEAN_NAME)
    fun dataQualityKafkaConsumerFactory(
        kafkaProperties: KafkaProperties,
        properties: DataQualityProperties,
    ): ConsumerFactory<String, String> {
        val consumerProperties = kafkaProperties.buildConsumerProperties()
        consumerProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProperties[ConsumerConfig.GROUP_ID_CONFIG] = properties.kafka.consumer.groupId
        return DefaultKafkaConsumerFactory(consumerProperties)
    }

    @Bean(DATA_QUALITY_KAFKA_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
    fun dataQualityKafkaListenerContainerFactory(
        @Qualifier(DATA_QUALITY_KAFKA_CONSUMER_FACTORY_BEAN_NAME)
        consumerFactory: ConsumerFactory<String, String>,
    ): ConcurrentKafkaListenerContainerFactory<String, String> =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            setConsumerFactory(consumerFactory)
            setCommonErrorHandler(DefaultErrorHandler(FixedBackOff(0L, 0L)))
        }

    @Bean
    fun kafkaDataQualityResultListener(
        objectMapper: ObjectMapper,
        ingestService: DataQualityResultIngestService,
    ): KafkaDataQualityResultListener =
        KafkaDataQualityResultListener(
            objectMapper = objectMapper,
            ingestService = ingestService,
        )

    companion object {
        const val DATA_QUALITY_KAFKA_CONSUMER_FACTORY_BEAN_NAME = "dataQualityKafkaConsumerFactory"
        const val DATA_QUALITY_KAFKA_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "dataQualityKafkaListenerContainerFactory"
    }
}
