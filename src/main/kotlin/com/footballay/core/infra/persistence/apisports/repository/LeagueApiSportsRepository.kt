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
    
    /**
     * API ID로 리그 조회
     */
    @EntityGraph(attributePaths = ["leagueCore"])
    fun findByApiId(apiId: Long): LeagueApiSports?
    
    /**
     * 현재 시즌이 설정된 모든 리그 조회
     */
    fun findAllByCurrentSeasonIsNotNull(): List<LeagueApiSports>
    
    /**
     * 국가 코드로 리그 조회
     */
    fun findByCountryCode(countryCode: String): List<LeagueApiSports>
}