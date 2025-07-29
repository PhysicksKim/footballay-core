package com.footballay.core.infra.persistence.apisports.repository.live

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ApiSportsMatchEventRepository : JpaRepository<ApiSportsMatchEvent, Long> {

    /**
     * 특정 경기의 모든 이벤트를 sequence 순으로 조회합니다.
     */
    @Query("SELECT e FROM ApiSportsMatchEvent e WHERE e.fixtureApi = :fixture ORDER BY e.sequence ASC")
    fun findByFixtureOrderBySequenceAsc(@Param("fixture") fixture: FixtureApiSports): List<ApiSportsMatchEvent>

    /**
     * 특정 경기의 모든 이벤트를 sequence 역순으로 조회합니다.
     */
    @Query("SELECT e FROM ApiSportsMatchEvent e WHERE e.fixtureApi = :fixture ORDER BY e.sequence DESC")
    fun findByFixtureOrderBySequenceDesc(@Param("fixture") fixture: FixtureApiSports): List<ApiSportsMatchEvent>

    /**
     * 특정 경기의 이벤트 개수를 조회합니다.
     */
    fun countByFixtureApi(fixture: FixtureApiSports): Long

    /**
     * 특정 경기의 특정 sequence 이후 이벤트들을 조회합니다.
     */
    @Query("SELECT e FROM ApiSportsMatchEvent e WHERE e.fixtureApi = :fixture AND e.sequence > :sequence ORDER BY e.sequence ASC")
    fun findByFixtureAndSequenceGreaterThanOrderBySequenceAsc(
        @Param("fixture") fixture: FixtureApiSports,
        @Param("sequence") sequence: Int
    ): List<ApiSportsMatchEvent>
}