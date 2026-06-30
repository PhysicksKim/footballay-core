package com.footballay.core.domain.model.match

/**
 * 경기 라이브 상태 도메인 모델
 *
 * Query Service → Web Layer 전달용 도메인 모델
 */
data class FixtureLiveStatusModel(
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
