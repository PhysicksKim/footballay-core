package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TeamApiSportsRepository : JpaRepository<TeamApiSports, Long> {
    fun findAllByApiIdIn(apiIds: List<Long>): List<TeamApiSports>

    @Query("SELECT t FROM TeamApiSports t LEFT JOIN FETCH t.teamCore WHERE t.apiId = :apiId")
    fun findTeamApiSportsByApiIdWithTeamCore(apiId: Long): TeamApiSports?
}