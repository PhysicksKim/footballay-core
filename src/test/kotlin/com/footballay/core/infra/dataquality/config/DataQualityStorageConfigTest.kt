package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.AWSRawResponseStorage
import com.footballay.core.infra.dataquality.raw.LocalRawResponseStorage
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

/**
 * 설정에 따라 [DataQualityStorageConfig] 가 특정 구현체 빈 등록 및 설정값 등을 올바르게 처리하는지 테스트합니다.
 */
class DataQualityStorageConfigTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityStorageConfig::class.java)

    @Test
    fun `does not register local storage when data quality is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=local",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponseStorage::class.java)
            }
    }

    @Test
    fun `does not register local storage when storage is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.storage.enabled=false",
                "footballay.data-quality.storage.type=local",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponseStorage::class.java)
            }
    }

    @Test
    fun `does not register local storage when type is noop`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=noop",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponseStorage::class.java)
            }
    }

    @Test
    fun `registers local storage when enabled and type is local`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=local",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponseStorage::class.java)
                assertThat(context.getBean(RawResponseStorage::class.java)).isInstanceOf(LocalRawResponseStorage::class.java)
            }
    }

    @Test
    fun `registers s3 storage and data quality s3 client when enabled and type is s3`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=s3",
                "footballay.data-quality.storage.bucket=footballay-data-quality",
                "footballay.data-quality.storage.region=ap-northeast-2",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponseStorage::class.java)
                assertThat(context.getBean(RawResponseStorage::class.java)).isInstanceOf(AWSRawResponseStorage::class.java)
            }
    }

    @Test
    fun `local storage wins over noop storage when both configs are loaded`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityStorageConfig::class.java, DataQualityNoopConfig::class.java)
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.storage.enabled=true",
                "footballay.data-quality.storage.type=local",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponseStorage::class.java)
                assertThat(context.getBean(RawResponseStorage::class.java)).isInstanceOf(LocalRawResponseStorage::class.java)
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig
}
