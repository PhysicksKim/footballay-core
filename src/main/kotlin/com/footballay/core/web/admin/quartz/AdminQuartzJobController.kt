package com.footballay.core.web.admin.quartz

import com.footballay.core.infra.scheduler.matchjob.MatchJobKeyFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Tag(name = "Admin - Quartz", description = "Quartz Job 조회용 Admin API")
@SecurityRequirement(name = "cookieAuth")
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/quartz")
class AdminQuartzJobController(
    private val scheduler: Scheduler,
) {
    @Operation(summary = "Quartz job 목록 조회")
    @GetMapping("/jobs")
    fun getJobs(
        @Parameter(description = "job group prefix filter", example = "league:match:")
        @RequestParam(required = false)
        groupPrefix: String?,
    ): List<AdminQuartzJobResponse> {
        val groupNames =
            scheduler.jobGroupNames
                .filter { groupPrefix == null || it.startsWith(groupPrefix) }

        return groupNames
            .flatMap { groupName -> scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)) }
            .sortedWith(compareBy<JobKey> { it.group }.thenBy { it.name })
            .map(::toResponse)
    }

    private fun toResponse(jobKey: JobKey): AdminQuartzJobResponse {
        val jobDetail = scheduler.getJobDetail(jobKey)
        val triggers = scheduler.getTriggersOfJob(jobKey)
        val parsedIdentity = MatchJobKeyFactory.parseJobKey(jobKey)?.let {
            AdminQuartzMatchJobIdentityResponse(
                owner = it.owner.name,
                phase = it.phase.name,
                leagueUid = it.leagueUid,
                fixtureUid = it.fixtureUid,
            )
        }

        return AdminQuartzJobResponse(
            jobName = jobKey.name,
            jobGroup = jobKey.group,
            jobClass = jobDetail?.jobClass?.name,
            parsedIdentity = parsedIdentity,
            triggers = triggers.map { trigger -> toTriggerResponse(trigger) },
        )
    }

    private fun toTriggerResponse(trigger: Trigger): AdminQuartzTriggerResponse {
        val simpleTrigger = trigger as? SimpleTrigger
        return AdminQuartzTriggerResponse(
            triggerName = trigger.key.name,
            triggerGroup = trigger.key.group,
            triggerState = scheduler.getTriggerState(trigger.key).name,
            startTime = trigger.startTime?.toInstant(),
            nextFireTime = trigger.nextFireTime?.toInstant(),
            previousFireTime = trigger.previousFireTime?.toInstant(),
            repeatIntervalMillis = simpleTrigger?.repeatInterval,
            repeatCount = simpleTrigger?.repeatCount,
            timesTriggered = simpleTrigger?.timesTriggered,
        )
    }
}

data class AdminQuartzJobResponse(
    val jobName: String,
    val jobGroup: String,
    val jobClass: String?,
    val parsedIdentity: AdminQuartzMatchJobIdentityResponse?,
    val triggers: List<AdminQuartzTriggerResponse>,
)

data class AdminQuartzMatchJobIdentityResponse(
    val owner: String,
    val phase: String,
    val leagueUid: String,
    val fixtureUid: String,
)

data class AdminQuartzTriggerResponse(
    val triggerName: String,
    val triggerGroup: String,
    val triggerState: String,
    val startTime: Instant?,
    val nextFireTime: Instant?,
    val previousFireTime: Instant?,
    val repeatIntervalMillis: Long?,
    val repeatCount: Int?,
    val timesTriggered: Int?,
)
