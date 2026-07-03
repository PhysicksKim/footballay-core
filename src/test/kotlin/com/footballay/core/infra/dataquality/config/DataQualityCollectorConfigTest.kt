package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.ApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.DefaultApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.RawResponseCanonicalHasher
import com.footballay.core.infra.dataquality.raw.RawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.RawResponseGzipCodec
import com.footballay.core.infra.dataquality.raw.RawResponseObjectKeyFactory
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

class DataQualityCollectorConfigTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityCollectorConfig::class.java)

    @Test
    fun `does not register collector when data quality is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.raw-collection.enabled=true",
            ).run { context ->
                assertThat(context).doesNotHaveBean(ApiSportsRawResponseCollector::class.java)
            }
    }

    @Test
    fun `does not register collector when raw collection is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.raw-collection.enabled=false",
            ).run { context ->
                assertThat(context).doesNotHaveBean(ApiSportsRawResponseCollector::class.java)
            }
    }

    @Test
    fun `registers collector when raw collection and executor are enabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.raw-collection.enabled=true",
            ).run { context ->
                assertThat(context).hasSingleBean(ApiSportsRawResponseCollector::class.java)
                assertThat(context.getBean(ApiSportsRawResponseCollector::class.java))
                    .isInstanceOf(DefaultApiSportsRawResponseCollector::class.java)
            }
    }

    @Test
    fun `collector wins over noop collector when both configs are loaded`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityCollectorConfig::class.java, DataQualityNoopConfig::class.java)
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.raw-collection.enabled=true",
            ).run { context ->
                assertThat(context).hasSingleBean(ApiSportsRawResponseCollector::class.java)
                assertThat(context.getBean(ApiSportsRawResponseCollector::class.java))
                    .isInstanceOf(DefaultApiSportsRawResponseCollector::class.java)
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig {
        @Bean(name = [DATA_QUALITY_TASK_EXECUTOR_BEAN_NAME])
        fun dataQualityTaskExecutor(): ThreadPoolTaskExecutor =
            ThreadPoolTaskExecutor().apply {
                corePoolSize = 1
                maxPoolSize = 1
                queueCapacity = 1
                initialize()
            }

        @Bean
        fun rawResponseCanonicalHasher(): RawResponseCanonicalHasher = mock()

        @Bean
        fun rawResponseDuplicateGate(): RawResponseDuplicateGate = mock()

        @Bean
        fun rawResponseObjectKeyFactory(): RawResponseObjectKeyFactory = mock()

        @Bean
        fun rawResponseGzipCodec(): RawResponseGzipCodec = mock()

        @Bean
        fun rawResponseStorage(): RawResponseStorage = mock()

        @Bean
        fun rawResponsePublisher(): RawResponsePublisher = mock()
    }
}
