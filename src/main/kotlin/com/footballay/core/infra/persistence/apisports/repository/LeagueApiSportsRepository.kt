package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LeagueApiSportsRepository : JpaRepository<LeagueApiSports, Long> {

    // findAllByApiIdIn
    fun findAllByApiIdIn(apiIds: List<Long>): List<LeagueApiSports>

    @Query(
        "SELECT l FROM LeagueApiSports l " +
        "LEFT JOIN FETCH l.leagueCore c " +
        "LEFT JOIN FETCH l.seasons s " +
        "WHERE l.apiId = :apiId"
    )
    fun findByApiIdIncludeCoreAndSeasons (apiId: Long): LeagueApiSports?

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
     * API ID와 시즌으로 리그와 시즌 정보를 함께 조회
     */
    @Query("""
        SELECT las FROM LeagueApiSports las
        LEFT JOIN FETCH las.leagueCore lc
        LEFT JOIN FETCH las.seasons lass
        WHERE las.apiId = :apiId AND lass.seasonYear = :seasonYear
    """)
    fun findByApiIdAndSeasonWithCoreAndSeasons(
        @Param("apiId") apiId: Long,
        @Param("seasonYear") seasonYear: String
    ): LeagueApiSports?
    
    /**
     * 현재 시즌이 설정된 모든 리그 조회
     */
    fun findAllByCurrentSeasonIsNotNull(): List<LeagueApiSports>
    
    /**
     * 국가 코드로 리그 조회
     */
    fun findByCountryCode(countryCode: String): List<LeagueApiSports>
}