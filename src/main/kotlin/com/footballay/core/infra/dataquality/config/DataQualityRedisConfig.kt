package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.RawResponseDuplicateGate
import com.footballay.core.infra.dataquality.raw.RedisRawResponseDuplicateGate
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate

@Configuration
@ConditionalOnProperty(
    prefix = "footballay.data-quality",
    name = ["enabled", "duplicate-gate.enabled"],
    havingValue = "true",
)
class DataQualityRedisConfig {
    @Bean
    @ConditionalOnBean(StringRedisTemplate::class)
    fun rawResponseDuplicateGate(
        stringRedisTemplate: StringRedisTemplate,
        properties: DataQualityProperties,
    ): RawResponseDuplicateGate =
        RedisRawResponseDuplicateGate(
            stringRedisTemplate = stringRedisTemplate,
            ttl = properties.duplicateGate.ttl,
        )
}
