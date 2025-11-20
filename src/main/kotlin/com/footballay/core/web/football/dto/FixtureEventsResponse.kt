package com.footballay.core.web.football.dto

/**
 * 경기 이벤트 목록 응답 DTO
 *
 * @param fixtureUid Fixture UID
 * @param events 경기 이벤트 목록 (골, 카드, 교체 등)
 */
data class FixtureEventsResponse(
    val fixtureUid: String,
    val events: List<EventInfo>,
) {
    data class EventInfo(
        val sequence: Int,
        val elapsed: Int,
        val extraTime: Int?,
        val team: TeamInfo,
        val player: PlayerInfo?,
        val assist: PlayerInfo?,
        val type: String, // Goal, Card, Subst, Var
        val detail: String, // Yellow Card, Red Card, Substitution 1, 2, 3, etc.
        val comments: String?,
    )

    data class TeamInfo(
        val teamUid: String,
        val name: String,
        val koreanName: String?,
    )

    data class PlayerInfo(
        val matchPlayerUid: String,
        val playerUid: String?,
        val name: String,
        val koreanName: String?,
        val number: Int?,
    )
}
