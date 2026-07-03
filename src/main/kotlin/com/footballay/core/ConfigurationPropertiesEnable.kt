package com.footballay.core

import com.footballay.core.infra.apisports.shared.config.ApiSportsProperties
import com.footballay.core.infra.dataquality.config.DataQualityProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    ApiSportsProperties::class,
    DataQualityProperties::class,
)
class ConfigurationPropertiesEnable

