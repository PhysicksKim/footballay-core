package com.footballay.core.infra.scheduler.config

import org.springframework.boot.context.properties.ConfigurationProperties

// Quartz 기반 스케줄 기능의 설정 메타데이터와 기본값을 제공합니다.
@ConfigurationProperties(prefix = "footballay.startup-match-job-cleanup")
class StartupMatchJobCleanupProperties {
    var enabled: Boolean = true

    companion object {
        const val PREFIX = "footballay.startup-match-job-cleanup"
        const val ENABLED = "enabled"
        const val ENABLED_PROPERTY = "$PREFIX.$ENABLED"
    }
}

@ConfigurationProperties(prefix = "footballay.fixture-schedule-update")
class FixtureScheduleUpdateProperties {
    var enabled: Boolean = true

    companion object {
        const val PREFIX = "footballay.fixture-schedule-update"
        const val ENABLED = "enabled"
        const val ENABLED_PROPERTY = "$PREFIX.$ENABLED"
    }
}

@ConfigurationProperties(prefix = "footballay.match-collect")
class MatchCollectProperties {
    var finishedScanner: FinishedScanner = FinishedScanner()

    class FinishedScanner {
        var enabled: Boolean = true
    }

    companion object {
        const val PREFIX = "footballay.match-collect"
        const val FINISHED_SCANNER_ENABLED = "finished-scanner.enabled"
        const val FINISHED_SCANNER_ENABLED_PROPERTY = "$PREFIX.$FINISHED_SCANNER_ENABLED"
    }
}
