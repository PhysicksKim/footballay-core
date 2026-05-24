package com.footballay.core.infra.scheduler.matchjob

import org.quartz.Job
import java.time.Instant

data class MatchJobSchedule(
    val jobClass: Class<out Job>,
    val startAt: Instant,
    val repeatIntervalSeconds: Int,
    val repeatCount: Int,
    val compareStartAt: Boolean = true,
) {
    val repeatIntervalMillis: Long = repeatIntervalSeconds * 1000L
}
