package com.footballay.core.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "refac_fixtures")
data class FixtureCore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    /**
     * 외부 노출 ID
     */
    var uid: String? = null,

    var date: LocalDateTime,

    var timestamp: Long,

    /**
     * 경기 상태 (예: Not Started, Match Finished, Live 등)
     */
    var status: String,

    var statusShort: String,

    var statusElapsed: Int? = null,

    /**
     * 리그 정보
     */
    var leagueId: Long,

    var season: Int,

    var round: String,

    /**
     * 팀 정보
     */
    var homeTeamId: Long,

    var awayTeamId: Long,

    /**
     * 경기 결과
     */
    var goalsHome: Int? = null,

    var goalsAway: Int? = null,

    var available: Boolean = false,

    /**
     * Core 엔티티에도 apiId를 두면, 동일 경기 중복여부 검사 시 편함
     */
    var apiId: Long? = null
)