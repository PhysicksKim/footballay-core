package com.footballay.core.domain.model

import java.time.Instant

sealed interface FixtureExtension

/**
 * 축구 경기 일정을 나타내는 도메인 모델
 */
data class FixtureModel(
    val uid: String,
    /**
     * 소속 리그의 고유 아이디.
     *
     * 편의를 위해서 fixture 의 소속 리그 정보를 가지고 다닐 수 있도록 둔 필드입니다.
     * 리그 정보를 Load 할 필요 없는 경우가 있어서 nullable로 둡니다.
     */
    val leagueUid: String?,
    val schedule: FixtureSchedule,
    /**
     * 경기의 팀이 "미정"인 경우 null 로 주어집니다.
     */
    val homeTeam: TeamSide?,
    /**
     * 경기의 팀이 "미정"인 경우 null 로 주어집니다.
     */
    val awayTeam: TeamSide?,
    val status: Status,
    val score: Score,
    val available: Boolean,
    val extension: FixtureExtension = NoFixtureExtension,
) {
    data class FixtureSchedule(
        /**
         * 시작 시간이 미정인 경우 null 로 주어집니다.
         */
        val kickoffAt: Instant?,
        val round: String,
        /**
         * "EPL 16R" 처럼 숫자로 표현 가능한 경우 추가로 제공
         *
         * - null로 주어지는 경우
         * "Tournament Round 4" 같은 경우 "Quarter Final - 4Teams" 와 혼동될 수 있으므로 null로 처리
         */
        val roundNum: Int? = null,
    )

    data class Status(
        /**
         * Provider 마다 Status 표현이 다를 수 있어서, 상태를 설명하기 위한 라벨에 해당합니다.
         * 긴 설명이 아닌 간략한 설명으로 주어집니다. (예: "1st Half", "Not Started" 등)
         */
        val statusText: String,
        /**
         * 경기 상태 타입 기반 약어
         */
        val code: StatusCode,
        /**
         * 경기 시간
         *
         * 시작하지 않았거나 값이 주어지지 않은 경우 null 입니다.
         */
        val elapsed: Int?,
        /**
         * 추가 시간 경과
         *
         * 추가시간이 주어지지 않은 경우 null 입니다.
         * 이 값은 "현재" 추가 시간 경과를 뜻합니다.
         * 주어진 추가시간이 5분이더라도 현재 추가시간 1분이면 1로 주어집니다.
         */
        val extra: Int?,
    )

    data class TeamSide(
        val uid: String,
        val name: String,
        val nameKo: String?,
        val logo: String?,
    )

    data class Score(
        val home: Int?,
        val away: Int?,
    )

    enum class StatusCode(
        val value: String,
    ) {
        NS("NS"),
        TBD("TBD"),
        FIRST_HALF("1H"),
        HT("HT"),
        SECOND_HALF("2H"),
        FT("FT"),
        ET("ET"),
        PST("PST"),
        CANC("CANC"),
        ETC("ETC"),
    }
}

object NoFixtureExtension : FixtureExtension

data class FixtureApiSportsExtension(
    val apiId: Long,
    val refereeName: String? = null,
) : FixtureExtension
