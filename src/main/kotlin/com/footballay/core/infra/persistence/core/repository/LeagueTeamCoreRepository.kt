package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.LeagueTeamCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LeagueTeamCoreRepository : JpaRepository<LeagueTeamCore, Long> {
    @Query(
        """
        SELECT DISTINCT relation.team
        FROM LeagueTeamCore relation
        WHERE relation.league.uid = :leagueUid
        ORDER BY relation.team.name
        """,
    )
    fun findTeamsByLeagueUid(
        @Param("leagueUid") leagueUid: String,
    ): List<TeamCore>

    fun findByLeagueIdAndTeamId(
        leagueId: Long,
        teamId: Long,
    ): List<LeagueTeamCore>

    fun findByLeagueId(leagueId: Long): List<LeagueTeamCore>

    fun deleteByLeagueIdAndTeamId(
        leagueId: Long,
        teamId: Long,
    )

    fun existsByLeagueIdAndTeamId(
        leagueId: Long,
        teamId: Long,
    ): Boolean
}
