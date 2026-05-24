package com.footballay.core.infra.scheduler.matchjob

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

/**
 * Match job이 무엇인지를 표현하는 Quartz 의존성 없는 논리 식별자입니다.
 */
data class MatchJobIdentity(
    val owner: MatchJobOwner,
    val phase: MatchJobPhase,
    val leagueUid: String,
    val fixtureUid: String,
)
