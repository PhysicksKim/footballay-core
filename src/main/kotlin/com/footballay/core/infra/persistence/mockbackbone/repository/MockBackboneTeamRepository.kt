package com.footballay.core.infra.persistence.mockbackbone.repository

import com.footballay.core.infra.persistence.mockbackbone.entity.MockBackboneTeam
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MockBackboneTeamRepository : JpaRepository<MockBackboneTeam, Long> {
    @Query(
        """
        SELECT mt
        FROM MockBackboneTeam mt
        JOIN FETCH mt.team t
        WHERE t.uid = :teamCoreUid
    """,
    )
    fun findByTeamCoreUid(
        @Param("teamCoreUid") teamCoreUid: String,
    ): MockBackboneTeam?

    fun findByScenarioUid(scenarioUid: String): List<MockBackboneTeam>
}
