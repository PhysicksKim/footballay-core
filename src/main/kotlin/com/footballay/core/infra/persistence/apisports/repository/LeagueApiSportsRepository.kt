package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface LeagueApiSportsRepository : JpaRepository<LeagueApiSports, Long> {

    // findAllByApiIdIn
    fun findAllByApiIdIn(apiIds: List<Long>): List<LeagueApiSports>

    @Query("SELECT l FROM LeagueApiSports l " +
            "LEFT JOIN FETCH l.leagueCore " +
            "WHERE l.apiId IN :apiIds")
    fun findLeagueApiSportsInApiId(apiIds: List<Long>): List<LeagueApiSports>

    fun findLeagueApiSportsByApiId(apiId: Long): LeagueApiSports?
}