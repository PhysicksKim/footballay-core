package com.footballay.core.infra.dataquality.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

const val DATA_QUALITY_TASK_EXECUTOR_BEAN_NAME = "dataQualityTaskExecutor"

@Configuration
@ConditionalOnProperty(
    prefix = "footballay.data-quality",
    name = ["enabled", "raw-collection.enabled"],
    havingValue = "true",
)
class DataQualityAsyncConfig {
    @Bean(name = [DATA_QUALITY_TASK_EXECUTOR_BEAN_NAME])
    fun dataQualityTaskExecutor(properties: DataQualityProperties): ThreadPoolTaskExecutor {
        val async = properties.async
        require(async.corePoolSize > 0) {
            "footballay.data-quality.async.core-pool-size must be greater than 0"
        }
        require(async.maxPoolSize >= async.corePoolSize) {
            "footballay.data-quality.async.max-pool-size must be greater than or equal to core-pool-size"
        }
        require(async.queueCapacity >= 0) {
            "footballay.data-quality.async.queue-capacity must be greater than or equal to 0"
        }

        return ThreadPoolTaskExecutor().apply {
            corePoolSize = async.corePoolSize
            maxPoolSize = async.maxPoolSize
            queueCapacity = async.queueCapacity
            setThreadNamePrefix("data-quality-")
            setWaitForTasksToCompleteOnShutdown(false)
            setAwaitTerminationSeconds(5)
            initialize()
        }
    }
}
