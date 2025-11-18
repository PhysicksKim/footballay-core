package com.footballay.core.domain.model.match

/**
 * 경기 기본 정보 도메인 모델
 *
 * Query Service → Web Layer 전달용 도메인 모델
 */
data class FixtureInfoModel(
    val fixtureUid: String,
    val referee: String?,
    val date: String,
    val league: LeagueInfo,
    val home: TeamInfo,
    val away: TeamInfo,
) {
    data class LeagueInfo(
        val id: Long = 0, // Deprecated: PK 노출 방지
        val name: String,
        val koreanName: String?,
        val logo: String?,
        val leagueUid: String,
    )

    data class TeamInfo(
        val id: Long = 0, // Deprecated: PK 노출 방지
        val name: String,
        val koreanName: String?,
        val logo: String?,
        val teamUid: String,
    )
}
