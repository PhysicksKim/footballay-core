package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FixtureApiSportsRepository : JpaRepository<FixtureApiSports, Long> {

    @EntityGraph(attributePaths = ["core"])
    fun findByCoreUid(coreUid: String): FixtureApiSports?

    @Query("SELECT f FROM FixtureApiSports f JOIN f.core c WHERE c.uid = :coreUid")
    fun findByCoreUidWithJpql(@Param("coreUid") coreUid: String): FixtureApiSports?
    
    fun findAllByApiIdIn(apiIds: List<Long>): List<FixtureApiSports>
    
    fun findByApiId(apiId: Long): FixtureApiSports?

    @Query("""
        SELECT f FROM FixtureApiSports f 
        JOIN FETCH f.core c 
        JOIN FETCH f.season s 
        LEFT JOIN FETCH f.venue v 
        LEFT JOIN FETCH f.events e 
        LEFT JOIN FETCH e.matchTeam em
        LEFT JOIN FETCH e.player ep
        LEFT JOIN FETCH e.assist ea
        WHERE f.apiId = :fixtureApiId
    """)
    fun findEventsByFixtureApiId(fixtureApiId: Long): FixtureApiSports?

    @Query("""
    SELECT f FROM FixtureApiSports f 
    LEFT JOIN FETCH f.homeTeam ht
    LEFT JOIN FETCH ht.teamStatistics 
    LEFT JOIN FETCH ht.players hp 
    LEFT JOIN FETCH hp.playerApiSports hpas 
    LEFT JOIN FETCH ht.teamApiSports tas 
    LEFT JOIN FETCH hp.statistics hps 
    WHERE f.apiId = :fixtureApiId
""")
    fun findFixtureHomeTeamLineupAndStats(fixtureApiId: Long): FixtureApiSports?

    @Query("""
    SELECT f FROM FixtureApiSports f 
    LEFT JOIN FETCH f.awayTeam at
    LEFT JOIN FETCH at.teamStatistics 
    LEFT JOIN FETCH at.players ap 
    LEFT JOIN FETCH ap.playerApiSports apas 
    LEFT JOIN FETCH at.teamApiSports tas 
    LEFT JOIN FETCH ap.statistics aps 
    WHERE f.apiId = :fixtureApiId
""")
    fun findFixtureAwayTeamLineupAndStats(fixtureApiId: Long): FixtureApiSports?

}