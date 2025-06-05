package com.footballay.core.infra.provider

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class FootballDataProviderFactory(
    @Value("\${footballay.provider.primary}") private val primaryProvider: String,
    @Value("\${footballay.provider.secondary}") private val secondaryProvider: String
) {
    
    @Bean
    @Primary
    fun primaryFootballDataProvider(
        apisportsProvider: ApisportsProvider,
        sportmonksProvider: SportmonksProvider
    ): FootballDataProvider {
        return when (primaryProvider.lowercase()) {
            "apisports" -> apisportsProvider
            "sportmonks" -> sportmonksProvider
            else -> throw IllegalArgumentException("알 수 없는 primary provider: $primaryProvider")
        }
    }

    @Bean("secondaryFootballDataProvider")
    fun secondaryFootballDataProvider(
        apisportsProvider: ApisportsProvider,
        sportmonksProvider: SportmonksProvider
    ): FootballDataProvider {
        return when (secondaryProvider.lowercase()) {
            "apisports" -> apisportsProvider
            "sportmonks" -> sportmonksProvider
            else -> throw IllegalArgumentException("알 수 없는 secondary provider: $secondaryProvider")
        }
    }
} 