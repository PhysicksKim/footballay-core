package com.footballay.core.infra.scheduler.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

class SchedulerFeaturePropertiesTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TestConfig::class.java))

    @Test
    fun `binds default enabled values`() {
        contextRunner.run { context ->
            assertThat(context.getBean(StartupMatchJobCleanupProperties::class.java).enabled).isTrue()
            assertThat(context.getBean(FixtureScheduleUpdateProperties::class.java).enabled).isTrue()
            assertThat(context.getBean(MatchCollectProperties::class.java).finishedScanner.enabled).isTrue()
        }
    }

    @Test
    fun `binds disabled values`() {
        contextRunner
            .withPropertyValues(
                "${StartupMatchJobCleanupProperties.ENABLED_PROPERTY}=false",
                "${FixtureScheduleUpdateProperties.ENABLED_PROPERTY}=false",
                "${MatchCollectProperties.FINISHED_SCANNER_ENABLED_PROPERTY}=false",
            ).run { context ->
                assertThat(context.getBean(StartupMatchJobCleanupProperties::class.java).enabled).isFalse()
                assertThat(context.getBean(FixtureScheduleUpdateProperties::class.java).enabled).isFalse()
                assertThat(context.getBean(MatchCollectProperties::class.java).finishedScanner.enabled).isFalse()
            }
    }

    @Configuration
    @EnableConfigurationProperties(
        StartupMatchJobCleanupProperties::class,
        FixtureScheduleUpdateProperties::class,
        MatchCollectProperties::class,
    )
    private class TestConfig
}
