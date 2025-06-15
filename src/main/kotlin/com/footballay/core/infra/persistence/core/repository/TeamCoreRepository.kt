package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.TeamCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TeamCoreRepository : JpaRepository<TeamCore, Long> {
    fun findByApiId(apiId: Long): TeamCore?

    @Query("SELECT t FROM TeamCore t LEFT JOIN FETCH t.leagueTeams WHERE t.apiId IN :apiIds")
    fun findAllByApiIdInWithLeagues(apiIds: List<Long>): List<TeamCore>
} 