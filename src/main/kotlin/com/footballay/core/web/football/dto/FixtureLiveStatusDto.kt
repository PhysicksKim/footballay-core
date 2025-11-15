package com.footballay.core.web.football.dto

/**
 * 경기 라이브 상태 응답 DTO
 *
 * @param fixtureUid Fixture UID
 * @param liveStatus 라이브 상태 정보 (스코어, 경기 시간, 진행 상태)
 */
data class FixtureLiveStatusDto(
    val fixtureUid: String,
    val liveStatus: LiveStatus,
) {
    data class LiveStatus(
        val elapsed: Int?,
        val shortStatus: String,
        val longStatus: String,
        val score: Score,
    )

    data class Score(
        val home: Int?,
        val away: Int?,
    )
}
