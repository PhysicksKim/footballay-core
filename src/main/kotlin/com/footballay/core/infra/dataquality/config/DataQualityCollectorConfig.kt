package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.ApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.DefaultApiSportsRawResponseCollector
import com.footballay.core.infra.dataquality.raw.RawResponseCanonicalHasher
import com.footballay.core.infra.dataquality.raw.RawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.RawResponseGzipCodec
import com.footballay.core.infra.dataquality.raw.RawResponseObjectKeyFactory
import com.footballay.core.infra.dataquality.raw.RawResponsePublisher
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@ConditionalOnProperty(
    prefix = "footballay.data-quality",
    name = ["enabled", "raw-collection.enabled"],
    havingValue = "true",
)
class DataQualityCollectorConfig {
    @Bean
    fun apiSportsRawResponseCollector(
        @Qualifier(DATA_QUALITY_TASK_EXECUTOR_BEAN_NAME)
        taskExecutor: ThreadPoolTaskExecutor,
        canonicalHasher: RawResponseCanonicalHasher,
        duplicateGate: RawResponseDuplicateGate,
        objectKeyFactory: RawResponseObjectKeyFactory,
        gzipCodec: RawResponseGzipCodec,
        storage: RawResponseStorage,
        publisher: RawResponsePublisher,
    ): ApiSportsRawResponseCollector =
        DefaultApiSportsRawResponseCollector(
            taskExecutor = taskExecutor,
            canonicalHasher = canonicalHasher,
            duplicateGate = duplicateGate,
            objectKeyFactory = objectKeyFactory,
            gzipCodec = gzipCodec,
            storage = storage,
            publisher = publisher,
        )
}
