package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.LocalRawResponseStorage
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Path

@Configuration
@ConditionalOnProperty(
    prefix = "footballay.data-quality",
    name = ["enabled", "storage.enabled"],
    havingValue = "true",
)
class DataQualityStorageConfig {
    @Bean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.storage",
        name = ["type"],
        havingValue = "local",
    )
    fun localRawResponseStorage(properties: DataQualityProperties): RawResponseStorage =
        LocalRawResponseStorage(
            baseDir = Path.of(properties.storage.localBaseDir),
            downloadUrlTtl = properties.storage.localDownloadUrlTtl,
        )
}
