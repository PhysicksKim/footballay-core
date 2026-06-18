package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LeagueApiSportsSeasonRepository : JpaRepository<LeagueApiSportsSeason, Long> {
    fun findAllByLeagueApiSports(leagueApiSports: LeagueApiSports): List<LeagueApiSportsSeason>

    @Query(
        """
        SELECT s
        FROM LeagueApiSportsSeason s
        LEFT JOIN FETCH s.leagueSeasonCore
        WHERE s.leagueApiSports IN :leagues
    """,
    )
    fun findAllByLeagueApiSportsInWithLeagueSeasonCore(
        @Param("leagues") leagues: Collection<LeagueApiSports>,
    ): List<LeagueApiSportsSeason>
}
