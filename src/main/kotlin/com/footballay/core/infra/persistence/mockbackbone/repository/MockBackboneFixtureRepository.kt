package com.footballay.core.infra.persistence.mockbackbone.repository

import com.footballay.core.infra.persistence.mockbackbone.entity.MockBackboneFixture
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MockBackboneFixtureRepository : JpaRepository<MockBackboneFixture, Long> {
    fun findAllByOrderByCreatedAtDesc(): List<MockBackboneFixture>

    @Query(
        """
        SELECT mf
        FROM MockBackboneFixture mf
        JOIN FETCH mf.fixture f
        JOIN FETCH f.league
        LEFT JOIN FETCH f.homeTeam
        LEFT JOIN FETCH f.awayTeam
        WHERE f.uid = :fixtureCoreUid
    """,
    )
    fun findByFixtureCoreUid(
        @Param("fixtureCoreUid") fixtureCoreUid: String,
    ): MockBackboneFixture?

    fun findByScenarioUid(scenarioUid: String): List<MockBackboneFixture>
}
