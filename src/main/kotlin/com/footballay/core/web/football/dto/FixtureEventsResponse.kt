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
    /**
     * 저장 단계에서 정규화된 이벤트입니다.
     *
     * `Goal + Own Goal`의 [team]은 득점 인정 팀이고 [player]는 자책골 선수라 다른 팀 소속일 수 있습니다.
     * `Goal + Missed Penalty`는 [type]이 `ETC`로 제공되므로 득점으로 처리하지 않습니다.
     */
    data class EventInfo(
        /** Core가 재정렬 후 부여한 순서이며 provider 배열 index가 아닙니다. */
        val sequence: Int,
        val elapsed: Int,
        val extraTime: Int?,
        /** 이벤트 귀속 팀입니다. Own Goal에서는 득점 인정 팀입니다. */
        val team: TeamInfo,
        val player: PlayerInfo?,
        val assist: PlayerInfo?,
        /** 정규화된 이벤트 타입입니다. 예. Goal, Card, Subst, Var, ETC. */
        val type: String,
        val detail: String, // Yellow Card, Red Card, Substitution 1, 2, 3, etc.
        val comments: String?,
    )

    data class TeamInfo(
        val teamUid: String,
        val name: String,
        val koreanName: String?,
        val playerColor: UniformColorDto?,
    )

    data class UniformColorDto(
        val primary: String?,
        val number: String?,
        val border: String?,
    )

    data class PlayerInfo(
        val matchPlayerUid: String,
        val playerUid: String?,
        val name: String,
        val koreanName: String?,
        val number: Int?,
    )
}
