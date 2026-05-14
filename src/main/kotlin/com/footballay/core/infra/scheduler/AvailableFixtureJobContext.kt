package com.footballay.core.infra.scheduler

import org.quartz.JobKey

enum class AvailableFixtureJobPhase {
    PRE_MATCH,
    LIVE_MATCH,
    POST_MATCH,
}

data class AvailableFixtureJobContext(
    val phase: AvailableFixtureJobPhase,
    val jobKey: JobKey,
)
