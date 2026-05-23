package com.footballay.core.infra.scheduler

import org.quartz.JobKey
import org.quartz.TriggerKey

enum class MatchJobOwner(
    val key: String,
) {
    AVAILABLE("available"),
}

enum class MatchJobPhase(
    val key: String,
) {
    PRE("pre"),
    LIVE("live"),
    POST("post"),
}

data class MatchJobIdentity(
    val owner: MatchJobOwner,
    val phase: MatchJobPhase,
    val leagueUid: String,
    val fixtureUid: String,
) {
    val groupName: String = groupName(leagueUid)
    val jobName: String = "${owner.key}:${phase.key}:$fixtureUid"
    val jobKey: JobKey = JobKey.jobKey(jobName, groupName)
    val triggerKey: TriggerKey = TriggerKey.triggerKey("$jobName:trigger", groupName)

    companion object {
        /*
         * Keep available and matchCollect jobs in the same league-level group so a league reconcile
         * can inspect every match-related job with one group query. Ownership is separated by the
         * job-name prefix (`available:*` vs `matchcollect:*`), so cleanup/reconcile code must filter
         * by owner before deleting or replacing jobs.
         */
        private const val GROUP_PREFIX = "league:match:"

        fun groupName(leagueUid: String): String = "$GROUP_PREFIX$leagueUid"

        fun leagueUidFromGroup(groupName: String): String? =
            groupName
                .takeIf { it.startsWith(GROUP_PREFIX) }
                ?.removePrefix(GROUP_PREFIX)
                ?.takeIf { it.isNotBlank() }

        fun isLeagueMatchGroup(groupName: String): Boolean = leagueUidFromGroup(groupName) != null

        fun isOwnerJobName(
            owner: MatchJobOwner,
            jobName: String,
        ): Boolean = jobName.startsWith("${owner.key}:")
    }
}
