package com.footballay.core.infra.scheduler.cleanup

import com.footballay.core.infra.scheduler.matchjob.MatchJobKeyFactory
import com.footballay.core.infra.scheduler.matchjob.MatchJobOwner
import com.footballay.core.logger
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.stereotype.Service

/**
 * 앱 시작 시 DB 기준 재생성을 위해 Quartz 에 남아 있는 available match job 을 삭제합니다.
 *
 * 이 클래스는 matchCollect 도입 전 available job key/group 규칙을 바꾸기 위한 마이그레이션성 cleanup 입니다.
 * 삭제 범위는 예전 group(`pre-match`, `live-match`, `post-match`)과 현재 group(`league:match:*`) 안의
 * available owner job 으로 제한합니다. 같은 group 의 matchCollect job 이나 legacy Java football scheduler job 은 삭제하지 않습니다.
 */
@Service
class StartupMatchJobCleanupService(
    private val scheduler: Scheduler,
) {
    private val log = logger()

    fun deleteAvailableMatchJobsForStartupRebuild(): MatchJobCleanupResult {
        val accumulator = MatchJobCleanupAccumulator()

        MatchJobKeyFactory.legacyAvailableJobGroups.forEach { groupName ->
            deleteJobsInGroup(
                groupName = groupName,
                scope = "legacy-available-group",
                accumulator = accumulator,
            )
        }

        val currentGroups =
            try {
                scheduler.jobGroupNames.filter(MatchJobKeyFactory::isLeagueMatchGroup)
            } catch (e: Exception) {
                accumulator.addCurrentGroupError(e)
                emptyList()
            }

        currentGroups.forEach { groupName ->
            deleteJobsInGroup(
                groupName = groupName,
                scope = "current-available-owner",
                accumulator = accumulator,
            ) { jobKey ->
                MatchJobKeyFactory.parseJobKey(jobKey)?.owner == MatchJobOwner.AVAILABLE
            }
        }

        val result = accumulator.toResult()
        log.info(
            "Startup available match job cleanup finished - deleted={}, skipped={}, success={}, errors={}",
            result.deleted,
            result.skipped,
            result.success,
            result.errors.size,
        )
        return result
    }

    private fun deleteJobsInGroup(
        groupName: String,
        scope: String,
        accumulator: MatchJobCleanupAccumulator,
        shouldDelete: (JobKey) -> Boolean = { true },
    ) {
        val jobKeys =
            try {
                scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))
            } catch (e: Exception) {
                val operation = "list-jobs:$scope"
                val message = e.message ?: e::class.simpleName.orEmpty()
                accumulator.addCleanupError(groupName, operation, message)
                return
            }

        jobKeys.forEach { jobKey ->
            if (!shouldDelete(jobKey)) {
                accumulator.skipped++
                return@forEach
            }

            try {
                if (scheduler.deleteJob(jobKey)) {
                    accumulator.deleted++
                } else {
                    val operation = "delete:$scope"
                    val message = "Quartz returned false while deleting job"
                    accumulator.addCleanupError(groupName, operation, message, jobKey)
                }
            } catch (e: Exception) {
                val operation = "delete:$scope"
                val message = e.message ?: e::class.simpleName.orEmpty()
                accumulator.addCleanupError(groupName, operation, message, jobKey)
            }
        }
    }
}

data class MatchJobCleanupError(
    val groupName: String?,
    val jobKey: JobKey?,
    val operation: String,
    val message: String,
)

data class MatchJobCleanupResult(
    val deleted: Int,
    val skipped: Int,
    val errors: List<MatchJobCleanupError>,
) {
    val success: Boolean
        get() = errors.isEmpty()
}

private data class MatchJobCleanupAccumulator(
    var deleted: Int = 0,
    var skipped: Int = 0,
    var errors: List<MatchJobCleanupError> = emptyList(),
) {
    fun toResult(): MatchJobCleanupResult =
        MatchJobCleanupResult(
            deleted = deleted,
            skipped = skipped,
            errors = errors,
        )

    fun addCurrentGroupError(e: Exception) {
        this.errors +=
            MatchJobCleanupError(
                groupName = null,
                jobKey = null,
                operation = "list-current-groups",
                message = e.message ?: e::class.simpleName.orEmpty(),
            )
    }

    fun addCleanupError(
        groupName: String,
        operation: String,
        message: String,
        jobKey: JobKey? = null,
    ) {
        this.errors +=
            MatchJobCleanupError(
                groupName = groupName,
                jobKey = jobKey,
                operation = operation,
                message = message,
            )
    }
}
