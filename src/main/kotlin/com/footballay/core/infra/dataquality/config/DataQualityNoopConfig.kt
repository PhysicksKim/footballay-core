package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.ApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.NoopApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.NoopRawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.NoopRawResponsePublisher
import com.footballay.core.infra.dataquality.raw.NoopRawResponseStorage
import com.footballay.core.infra.dataquality.raw.RawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataQualityNoopConfig {
    @Bean
    @ConditionalOnMissingBean
    fun apiSportsRawResponseCollector(): ApiSportsRawResponseCollector = NoopApiSportsRawResponseCollector()

    @Bean
    @ConditionalOnMissingBean
    fun rawResponseDuplicateGate(): RawResponseDuplicateGate = NoopRawResponseDuplicateGate()

    @Bean
    @ConditionalOnMissingBean
    fun rawResponseStorage(): RawResponseStorage = NoopRawResponseStorage()

    @Bean
    @ConditionalOnMissingBean
    fun rawResponsePublisher(): RawResponsePublisher = NoopRawResponsePublisher()
}
