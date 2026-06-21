package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FixtureMatchCollectStateRepository : JpaRepository<FixtureMatchCollectState, Long> {
    fun findByFixture_Uid(fixtureUid: String): FixtureMatchCollectState?

    fun existsByFixture_Uid(fixtureUid: String): Boolean

    @EntityGraph(attributePaths = ["fixture", "fixture.league", "fixture.leagueSeason", "fixture.leagueSeason.league"])
    @Query(
        """
        SELECT s
        FROM FixtureMatchCollectState s
        JOIN s.fixture f
        LEFT JOIN f.league legacyLeague
        LEFT JOIN f.leagueSeason ls
        LEFT JOIN ls.league l
        WHERE (:leagueUid IS NULL OR l.uid = :leagueUid OR legacyLeague.uid = :leagueUid)
          AND (:fixtureUid IS NULL OR f.uid = :fixtureUid)
          AND (:status IS NULL OR s.matchCollectStatus = :status)
          AND (:incompleteOnly = false OR s.matchCollectStatus = com.footballay.core.domain.matchcollect.MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN)
        ORDER BY f.kickoff DESC NULLS LAST, f.id DESC
    """,
    )
    fun findAdminStates(
        @Param("leagueUid") leagueUid: String?,
        @Param("fixtureUid") fixtureUid: String?,
        @Param("status") status: MatchCollectStatus?,
        @Param("incompleteOnly") incompleteOnly: Boolean,
        pageable: Pageable,
    ): Page<FixtureMatchCollectState>
}
