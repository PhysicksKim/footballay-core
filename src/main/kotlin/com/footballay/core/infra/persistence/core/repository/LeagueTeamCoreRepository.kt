package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.LeagueTeamCore
import org.springframework.data.jpa.repository.JpaRepository

interface LeagueTeamCoreRepository : JpaRepository<LeagueTeamCore, Long> {
    fun findByLeagueIdAndTeamId(leagueId: Long, teamId: Long): List<LeagueTeamCore>
    fun deleteByLeagueIdAndTeamId(leagueId: Long, teamId: Long)
    fun existsByLeagueIdAndTeamId(leagueId: Long, teamId: Long): Boolean
}
