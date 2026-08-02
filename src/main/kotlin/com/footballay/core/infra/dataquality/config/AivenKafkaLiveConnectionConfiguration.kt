package com.footballay.core.infra.dataquality.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile

/** live 환경에서 Aiven Kafka 연결에 필요한 환경변수 누락을 기동 시점에 차단한다. */
@Configuration
@Profile("live")
@Lazy(false)
class AivenKafkaLiveConnectionConfiguration(
    @Value("\${AIVEN_KAFKA_BOOTSTRAP_SERVERS}") private val bootstrapServers: String,
    @Value("\${AIVEN_KAFKA_CA_PATH}") private val caPath: String,
    @Value("\${AIVEN_KAFKA_CORE_USERNAME}") private val username: String,
    @Value("\${AIVEN_KAFKA_CORE_PASSWORD}") private val password: String,
) {
    init {
        require(bootstrapServers.isNotBlank()) { "AIVEN_KAFKA_BOOTSTRAP_SERVERS must not be blank for live profile" }
        require(caPath.isNotBlank()) { "AIVEN_KAFKA_CA_PATH must not be blank for live profile" }
        require(username.isNotBlank()) { "AIVEN_KAFKA_CORE_USERNAME must not be blank for live profile" }
        require(password.isNotBlank()) { "AIVEN_KAFKA_CORE_PASSWORD must not be blank for live profile" }
    }
}
