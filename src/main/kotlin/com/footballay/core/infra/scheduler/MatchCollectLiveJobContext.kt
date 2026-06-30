package com.footballay.core.infra.scheduler

import org.quartz.JobKey

enum class MatchCollectLiveJobPhase {
    PRE,
    LIVE,
    POST,
}

data class MatchCollectLiveJobContext(
    val phase: MatchCollectLiveJobPhase,
    val jobKey: JobKey,
)
