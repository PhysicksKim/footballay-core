package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.ApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.NoopApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.NoopRawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.NoopRawResponseDownloadUrlGenerator
import com.footballay.core.infra.dataquality.raw.NoopRawResponsePublisher
import com.footballay.core.infra.dataquality.raw.NoopRawResponseStorage
import com.footballay.core.infra.dataquality.raw.RawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.RawResponseDownloadUrlGenerator
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataQualityNoopConfig {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.raw-collection",
        name = ["enabled"],
        havingValue = "false",
        matchIfMissing = true,
    )
    fun apiSportsRawResponseCollector(): ApiSportsRawResponseCollector = NoopApiSportsRawResponseCollector()

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.duplicate-gate",
        name = ["enabled"],
        havingValue = "false",
        matchIfMissing = true,
    )
    fun rawResponseDuplicateGate(): RawResponseDuplicateGate = NoopRawResponseDuplicateGate()

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.storage",
        name = ["enabled"],
        havingValue = "false",
        matchIfMissing = true,
    )
    fun rawResponseStorage(): RawResponseStorage = NoopRawResponseStorage()

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "footballay.data-quality.storage", name = ["enabled"], havingValue = "false", matchIfMissing = true)
    fun rawResponseDownloadUrlGenerator(): RawResponseDownloadUrlGenerator = NoopRawResponseDownloadUrlGenerator()

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.kafka.producer",
        name = ["enabled"],
        havingValue = "false",
        matchIfMissing = true,
    )
    fun rawResponsePublisher(): RawResponsePublisher = NoopRawResponsePublisher()
}
