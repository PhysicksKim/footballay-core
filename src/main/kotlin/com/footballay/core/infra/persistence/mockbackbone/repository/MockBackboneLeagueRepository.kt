package com.footballay.core.infra.persistence.mockbackbone.repository

import com.footballay.core.infra.persistence.mockbackbone.entity.MockBackboneLeague
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MockBackboneLeagueRepository : JpaRepository<MockBackboneLeague, Long> {
    @Query(
        """
        SELECT ml
        FROM MockBackboneLeague ml
        JOIN FETCH ml.league l
        WHERE l.uid = :leagueCoreUid
    """,
    )
    fun findByLeagueCoreUid(
        @Param("leagueCoreUid") leagueCoreUid: String,
    ): MockBackboneLeague?

    fun findByScenarioUid(scenarioUid: String): List<MockBackboneLeague>
}
