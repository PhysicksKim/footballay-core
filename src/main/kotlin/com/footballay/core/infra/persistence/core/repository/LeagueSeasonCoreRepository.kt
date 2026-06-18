package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueSeasonCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LeagueSeasonCoreRepository : JpaRepository<LeagueSeasonCore, Long> {
    fun findAllByLeague(league: LeagueCore): List<LeagueSeasonCore>

    @Query(
        """
        SELECT s
        FROM LeagueSeasonCore s
        WHERE s.league IN :leagues
    """,
    )
    fun findAllByLeagueIn(
        @Param("leagues") leagues: Collection<LeagueCore>,
    ): List<LeagueSeasonCore>

    fun findByLeagueAndSeasonYear(
        league: LeagueCore,
        seasonYear: Int,
    ): LeagueSeasonCore?

    fun findByLeagueAndCurrentTrue(league: LeagueCore): List<LeagueSeasonCore>
}
