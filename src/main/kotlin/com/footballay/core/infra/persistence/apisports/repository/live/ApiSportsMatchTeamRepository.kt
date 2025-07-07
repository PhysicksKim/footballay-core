package com.footballay.core.infra.persistence.apisports.repository.live

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ApiSportsMatchTeamRepository : JpaRepository<ApiSportsMatchTeam, String>  {
//
//    @Query("""
//        SELECT f.homeTeam FROM FixtureApiSports f
//        LEFT JOIN FETCH f.homeTeam ht
//        LEFT JOIN FETCH ht.teamStatistics
//        LEFT JOIN FETCH ht.players hp
//        LEFT JOIN FETCH hp.playerApiSports hpas
//        LEFT JOIN FETCH ht.teamApiSports tas
//        LEFT JOIN FETCH hp.statistics hps
//        WHERE f.apiId = :fixtureApiId
//    """)
//    fun findFixtureHomeTeamLineupAndStats(fixtureApiId: Long): ApiSportsMatchTeam?
//
//    @Query("""
//        SELECT f.awayTeam FROM FixtureApiSports f
//        LEFT JOIN FETCH f.awayTeam at
//        LEFT JOIN FETCH at.teamStatistics
//        LEFT JOIN FETCH at.players ap
//        LEFT JOIN FETCH ap.playerApiSports apas
//        LEFT JOIN FETCH at.teamApiSports tas
//        LEFT JOIN FETCH ap.statistics aps
//        WHERE f.apiId = :fixtureApiId
//    """)
//    fun findFixtureAwayTeamLineupAndStats(fixtureApiId: Long): ApiSportsMatchTeam?

}