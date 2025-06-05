package com.footballay.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    // 빈 이름을 "restClient"로 주거나, 완전히 다른 이름으로 바꿔버리면 충돌이 사라집니다.
    @Bean
    fun restClient(): RestClient {
        return RestClient.create()
    }
}