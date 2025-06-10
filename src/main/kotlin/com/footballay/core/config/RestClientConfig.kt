package com.footballay.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig (
    private val objectMapper: ObjectMapper
){

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder()
            .messageConverters{
                it.add(MappingJackson2HttpMessageConverter(objectMapper))
            }
            .build()
    }
}