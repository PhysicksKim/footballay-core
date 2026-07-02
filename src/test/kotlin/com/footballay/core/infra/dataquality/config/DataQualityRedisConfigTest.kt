package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.RawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.RedisRawResponseDuplicateGate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.function.Supplier

class DataQualityRedisConfigTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityRedisConfig::class.java)
            .withBean(StringRedisTemplate::class.java, Supplier { mock<StringRedisTemplate>() })

    @Test
    fun `does not register redis duplicate gate when data quality is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.duplicate-gate.enabled=true",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponseDuplicateGate::class.java)
            }
    }

    @Test
    fun `does not register redis duplicate gate when duplicate gate is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.duplicate-gate.enabled=false",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponseDuplicateGate::class.java)
            }
    }

    @Test
    fun `does not register redis duplicate gate without string redis template`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityRedisConfig::class.java)
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.duplicate-gate.enabled=true",
            ).run { context ->
                assertThat(context).doesNotHaveBean(RawResponseDuplicateGate::class.java)
            }
    }

    @Test
    fun `registers redis duplicate gate when enabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.duplicate-gate.enabled=true",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponseDuplicateGate::class.java)
                assertThat(context.getBean(RawResponseDuplicateGate::class.java))
                    .isInstanceOf(RedisRawResponseDuplicateGate::class.java)
            }
    }

    @Test
    fun `redis duplicate gate wins over noop duplicate gate when both configs are loaded`() {
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityRedisConfig::class.java, DataQualityNoopConfig::class.java)
            .withBean(StringRedisTemplate::class.java, Supplier { mock<StringRedisTemplate>() })
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.duplicate-gate.enabled=true",
            ).run { context ->
                assertThat(context).hasSingleBean(RawResponseDuplicateGate::class.java)
                assertThat(context.getBean(RawResponseDuplicateGate::class.java))
                    .isInstanceOf(RedisRawResponseDuplicateGate::class.java)
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig
}
