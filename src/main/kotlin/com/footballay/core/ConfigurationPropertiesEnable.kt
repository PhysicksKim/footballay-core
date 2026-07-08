package com.footballay.core

import com.footballay.core.infra.apisports.shared.config.ApiSportsProperties
import com.footballay.core.infra.dataquality.config.DataQualityProperties
import com.footballay.core.infra.scheduler.config.FixtureScheduleUpdateProperties
import com.footballay.core.infra.scheduler.config.MatchCollectProperties
import com.footballay.core.infra.scheduler.config.StartupMatchJobCleanupProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    ApiSportsProperties::class,
    DataQualityProperties::class,
    StartupMatchJobCleanupProperties::class,
    FixtureScheduleUpdateProperties::class,
    MatchCollectProperties::class,
)
class ConfigurationPropertiesEnable
