package com.footballay.core.infra.apisports.match.sync.dto

import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator

data class LineupSyncDto(
    val home: Lineup?,
    val away: Lineup?,
) {
    data class Lineup(
        val teamApiId: Long?,
        val teamName: String?,
        val teamLogo: String?,
        val playerColor: Color,
        val goalkeeperColor: Color,
        val formation: String?,
        /**
         * 해당 팀의 [com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator] 에서 생성한 MatchPlayer 키 목록입니다.
         * [com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext] 에서 실질적으로 값을 담은 DTO 또는 Entity 를 가져올 수 있습니다.
         */
        val startMpKeys: List<String>,
        val subMpKeys: List<String>,
    )

    data class Color(
        val primary: String?,
        val number: String?,
        val border: String?,
    )

    fun isEmpty(): Boolean = home == null || away == null

    /**
     * 완전한 라인업 여부를 확인합니다.
     *
     * "완전한 라인업"의 기준:
     * - home, away 팀 모두 라인업이 존재
     * - 모든 선발/후보 선수가 apiId를 가지고 있음 (ID 기반 키 사용)
     *
     * **배경:**
     * 유스 콜업 등으로 초기에 id=null인 선수가 등장할 수 있으며,
     * 이 경우 MatchPlayerKey는 "mp_name_" prefix를 사용합니다.
     * 경기 시작 전에 해당 선수에게 id가 부여되면 "mp_id_" prefix로 변경되며,
     * 이때 라인업이 "완전"해집니다.
     *
     * @return 모든 선수가 ID 기반 키를 가지고 있으면 true, 아니면 false
     */
    fun hasCompleteLineup(): Boolean {
        if (isEmpty()) return false

        val allLineupKeys = mutableListOf<String>()

        home?.let {
            allLineupKeys.addAll(it.startMpKeys)
            allLineupKeys.addAll(it.subMpKeys)
        }

        away?.let {
            allLineupKeys.addAll(it.startMpKeys)
            allLineupKeys.addAll(it.subMpKeys)
        }

        // 모든 라인업 선수의 키가 ID 기반인지 확인
        return allLineupKeys.all { key ->
            MatchPlayerKeyGenerator.isIdBasedKey(key)
        }
    }

    companion object {
        val EMPTY = LineupSyncDto(null, null)
    }
}

