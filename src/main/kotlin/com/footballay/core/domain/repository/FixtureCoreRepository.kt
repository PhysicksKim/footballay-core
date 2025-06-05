package com.footballay.core.domain.repository

import com.footballay.core.domain.entity.FixtureCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface FixtureCoreRepository : JpaRepository<FixtureCore, Long> {
    fun findByApiId(apiId: Long): FixtureCore?
    fun findByUid(uid: String): FixtureCore?
    fun findByLeagueIdAndSeason(leagueId: Long, season: Int): List<FixtureCore>
    fun findByHomeTeamIdOrAwayTeamId(homeTeamId: Long, awayTeamId: Long): List<FixtureCore>
    
    @Query("SELECT f FROM FixtureCore f WHERE f.date BETWEEN :startDate AND :endDate")
    fun findByDateBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<FixtureCore>
} 