package com.footballay.core.infra.dataquality.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

class DataQualityAsyncConfigTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java, DataQualityAsyncConfig::class.java)

    @Test
    fun `does not register executor when data quality is disabled`() {
        contextRunner.run { context ->
            assertThat(context).doesNotHaveBean(DATA_QUALITY_TASK_EXECUTOR_BEAN_NAME)
            assertThat(context).doesNotHaveBean(ThreadPoolTaskExecutor::class.java)
        }
    }

    @Test
    fun `does not register executor when raw collection is disabled`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.raw-collection.enabled=false",
            ).run { context ->
                assertThat(context).doesNotHaveBean(DATA_QUALITY_TASK_EXECUTOR_BEAN_NAME)
                assertThat(context).doesNotHaveBean(ThreadPoolTaskExecutor::class.java)
            }
    }

    @Test
    fun `registers configured data quality task executor`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.raw-collection.enabled=true",
                "footballay.data-quality.async.core-pool-size=3",
                "footballay.data-quality.async.max-pool-size=6",
                "footballay.data-quality.async.queue-capacity=50",
            ).run { context ->
                assertThat(context).hasBean(DATA_QUALITY_TASK_EXECUTOR_BEAN_NAME)
                val executor = context.getBean(DATA_QUALITY_TASK_EXECUTOR_BEAN_NAME, ThreadPoolTaskExecutor::class.java)

                assertThat(executor.corePoolSize).isEqualTo(3)
                assertThat(executor.maxPoolSize).isEqualTo(6)
                assertThat(executor.threadNamePrefix).isEqualTo("data-quality-")
                assertThat(executor.threadPoolExecutor.queue.remainingCapacity()).isEqualTo(50)
            }
    }

    @Test
    fun `rejects invalid executor configuration`() {
        contextRunner
            .withPropertyValues(
                "footballay.data-quality.enabled=true",
                "footballay.data-quality.raw-collection.enabled=true",
                "footballay.data-quality.async.core-pool-size=4",
                "footballay.data-quality.async.max-pool-size=2",
            ).run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure)
                    .hasRootCauseInstanceOf(IllegalArgumentException::class.java)
                    .hasMessageContaining("max-pool-size must be greater than or equal to core-pool-size")
            }
    }

    @Configuration
    @EnableConfigurationProperties(DataQualityProperties::class)
    private class TestConfig
}
