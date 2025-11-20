package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.FixtureCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface FixtureCoreRepository : JpaRepository<FixtureCore, Long> {
    fun findByUid(fixtureUid: String): FixtureCore

    /**
     * 특정 리그의 킥오프 시간 범위 내 Fixture들을 조회합니다.
     *
     * @param leagueId 리그 ID
     * @param startInclusive 시작 시각 (inclusive)
     * @param endExclusive 종료 시각 (exclusive)
     * @return 킥오프 시간 순으로 정렬된 Fixture 리스트
     */
    @Query(
        """
        SELECT f
        FROM FixtureCore f
        WHERE f.league.id = :leagueId
          AND f.kickoff >= :startInclusive
          AND f.kickoff < :endExclusive
        ORDER BY f.kickoff ASC
    """,
    )
    fun findFixturesInKickoffRange(
        @Param("leagueId") leagueId: Long,
        @Param("startInclusive") startInclusive: Instant,
        @Param("endExclusive") endExclusive: Instant,
    ): List<FixtureCore>

    /**
     * 특정 리그에서 from 이후 가장 가까운 kickoff 시각을 조회합니다.
     *
     * @param leagueId 리그 ID
     * @param from 기준 시각
     * @return 가장 가까운 kickoff 시각, 없으면 null
     */
    @Query(
        """
        SELECT MIN(f.kickoff)
        FROM FixtureCore f
        WHERE f.league.id = :leagueId
          AND f.kickoff >= :from
    """,
    )
    fun findMinKickoffAfter(
        @Param("leagueId") leagueId: Long,
        @Param("from") from: Instant,
    ): Instant?

    /**
     * 특정 리그(UID 기반)의 킥오프 시간 범위 내 Fixture들을 조회합니다.
     *
     * @param leagueUid 리그 UID
     * @param startInclusive 시작 시각 (inclusive)
     * @param endExclusive 종료 시각 (exclusive)
     * @return 킥오프 시간 순으로 정렬된 Fixture 리스트
     */
    @Query(
        """
        SELECT f
        FROM FixtureCore f
        LEFT JOIN FETCH f.homeTeam AS ht    
        LEFT JOIN FETCH f.awayTeam AS at
        LEFT JOIN FETCH ht.teamApiSports
        LEFT JOIN FETCH at.teamApiSports
        WHERE f.league.uid = :leagueUid
          AND f.kickoff >= :startInclusive
          AND f.kickoff < :endExclusive
        ORDER BY f.kickoff ASC
    """,
    )
    fun findFixturesByLeagueUidInKickoffRange(
        @Param("leagueUid") leagueUid: String,
        @Param("startInclusive") startInclusive: Instant,
        @Param("endExclusive") endExclusive: Instant,
    ): List<FixtureCore>

    /**
     * 특정 리그(UID 기반)에서 from 이후 가장 가까운 kickoff 시각을 조회합니다.
     *
     * @param leagueUid 리그 UID
     * @param from 기준 시각
     * @return 가장 가까운 kickoff 시각, 없으면 null
     */
    @Query(
        """
        SELECT MIN(f.kickoff)
        FROM FixtureCore f
        WHERE f.league.uid = :leagueUid
          AND f.kickoff >= :from
    """,
    )
    fun findMinKickoffAfterByLeagueUid(
        @Param("leagueUid") leagueUid: String,
        @Param("from") from: Instant,
    ): Instant?
}
