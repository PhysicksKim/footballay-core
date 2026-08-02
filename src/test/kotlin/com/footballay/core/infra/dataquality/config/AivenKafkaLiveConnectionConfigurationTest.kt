package com.footballay.core.infra.dataquality.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.MapPropertySource

class AivenKafkaLiveConnectionConfigurationTest {
    @Test
    fun `fails startup when Aiven Kafka environment variables are missing in live profile`() {
        AnnotationConfigApplicationContext().use { context ->
            context.environment.setActiveProfiles("live")
            context.addBeanFactoryPostProcessor(
                PropertySourcesPlaceholderConfigurer().apply {
                    setEnvironment(context.environment)
                },
            )
            context.register(AivenKafkaLiveConnectionConfiguration::class.java)

            assertThatThrownBy { context.refresh() }
                .hasRootCauseMessage(
                    "Could not resolve placeholder 'AIVEN_KAFKA_BOOTSTRAP_SERVERS' in value \"\${AIVEN_KAFKA_BOOTSTRAP_SERVERS}\"",
                )
        }
    }

    @Test
    fun `starts when all Aiven Kafka environment values are configured in live profile`() {
        AnnotationConfigApplicationContext().use { context ->
            context.environment.setActiveProfiles("live")
            context.addBeanFactoryPostProcessor(
                PropertySourcesPlaceholderConfigurer().apply {
                    setEnvironment(context.environment)
                },
            )
            context.environment.propertySources.addFirst(
                MapPropertySource(
                    "aivenKafkaTest",
                    mapOf<String, Any>(
                        "AIVEN_KAFKA_BOOTSTRAP_SERVERS" to "aiven.example.test:12345",
                        "AIVEN_KAFKA_CA_PATH" to "/run/secrets/aiven-kafka-ca.pem",
                        "AIVEN_KAFKA_CORE_USERNAME" to "example-user",
                        "AIVEN_KAFKA_CORE_PASSWORD" to "example-password",
                    ),
                ),
            )
            context.register(AivenKafkaLiveConnectionConfiguration::class.java)
            context.refresh()

            assertThat(context.getBeansOfType(AivenKafkaLiveConnectionConfiguration::class.java)).hasSize(1)
        }
    }
}
