package com.footballay.core.infra.scheduler

import org.quartz.JobKey
import org.quartz.TriggerKey

/**
 * Match job의 논리 식별자인 [MatchJobIdentity]와 Quartz JobKey/TriggerKey 사이의 변환 규칙을 제공합니다.
 */
object MatchJobKeyFactory {
    private const val LEAGUE_MATCH_GROUP_PREFIX = "league:match:"
    private const val TRIGGER_SUFFIX = ":trigger"

    private const val LEGACY_PRE_MATCH_GROUP = "pre-match"
    private const val LEGACY_LIVE_MATCH_GROUP = "live-match"
    private const val LEGACY_POST_MATCH_GROUP = "post-match"

    val legacyAvailableJobGroups: List<String> =
        listOf(
            LEGACY_PRE_MATCH_GROUP,
            LEGACY_LIVE_MATCH_GROUP,
            LEGACY_POST_MATCH_GROUP,
        )

    fun leagueMatchGroup(leagueUid: String): String = "$LEAGUE_MATCH_GROUP_PREFIX$leagueUid"

    fun leagueUidFromGroup(groupName: String): String? =
        groupName
            .takeIf { it.startsWith(LEAGUE_MATCH_GROUP_PREFIX) }
            ?.removePrefix(LEAGUE_MATCH_GROUP_PREFIX)
            ?.takeIf { it.isNotBlank() }

    fun isLeagueMatchGroup(groupName: String): Boolean = leagueUidFromGroup(groupName) != null

    fun jobKey(identity: MatchJobIdentity): JobKey =
        JobKey.jobKey(
            jobName(identity.owner, identity.phase, identity.fixtureUid),
            leagueMatchGroup(identity.leagueUid),
        )

    fun triggerKey(identity: MatchJobIdentity): TriggerKey =
        TriggerKey.triggerKey(
            triggerName(jobName(identity.owner, identity.phase, identity.fixtureUid)),
            leagueMatchGroup(identity.leagueUid),
        )

    fun parseJobKey(jobKey: JobKey): MatchJobIdentity? {
        val leagueUid = leagueUidFromGroup(jobKey.group) ?: return null
        val parts = jobKey.name.split(":")
        if (parts.size != 3) {
            return null
        }

        val owner = MatchJobOwner.entries.firstOrNull { it.key == parts[0] } ?: return null
        val phase = MatchJobPhase.entries.firstOrNull { it.key == parts[1] } ?: return null
        val fixtureUid = parts[2].takeIf { it.isNotBlank() } ?: return null

        return MatchJobIdentity(
            owner = owner,
            phase = phase,
            leagueUid = leagueUid,
            fixtureUid = fixtureUid,
        )
    }

    fun parseTriggerKey(triggerKey: TriggerKey): MatchJobIdentity? {
        val jobName =
            triggerKey.name
                .takeIf { it.endsWith(TRIGGER_SUFFIX) }
                ?.removeSuffix(TRIGGER_SUFFIX)
                ?: return null

        return parseJobKey(JobKey.jobKey(jobName, triggerKey.group))
    }

    fun jobName(
        owner: MatchJobOwner,
        phase: MatchJobPhase,
        fixtureUid: String,
    ): String = "${owner.key}:${phase.key}:$fixtureUid"

    fun triggerName(jobName: String): String = "$jobName$TRIGGER_SUFFIX"

    fun legacyAvailableJobKey(
        groupName: String,
        fixtureUid: String,
    ): JobKey = JobKey.jobKey("$groupName-$fixtureUid", groupName)
}
