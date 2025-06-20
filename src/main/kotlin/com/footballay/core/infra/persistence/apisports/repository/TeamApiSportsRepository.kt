package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.core.entity.TeamCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TeamApiSportsRepository : JpaRepository<TeamApiSports, Long> {
    fun findAllByApiIdIn(apiIds: List<Long>): List<TeamApiSports>

    @Query("SELECT t FROM TeamApiSports t LEFT JOIN FETCH t.teamCore WHERE t.apiId = :apiId")
    fun findTeamApiSportsByApiIdWithTeamCore(apiId: Long): TeamApiSports?
    
    fun findTeamApiSportsByApiId(apiId: Long): TeamApiSports?

    @Query("SELECT ta FROM TeamApiSports ta " +
            "LEFT JOIN FETCH ta.teamCore tc " +
            "WHERE tc IN :teamCores")
    fun findTeamApiSportsInTeamCore(teamCores: List<TeamCore>) : List<TeamApiSports>

    @Query("SELECT ta FROM TeamApiSports ta " +
            "JOIN ta.teamCore tc " +
            "JOIN LeagueTeamCore ltc ON ltc.team = tc " +
            "WHERE ltc.league.id = :leagueId")
    fun findAllByLeagueId(leagueId: Long): List<TeamApiSports>
}