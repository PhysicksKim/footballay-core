package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.TeamPlayerCore
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TeamPlayerCoreRepository : JpaRepository<TeamPlayerCore, Long> {
    @Query(
        """
        SELECT DISTINCT relation.player
        FROM TeamPlayerCore relation
        WHERE relation.team.uid = :teamUid
        ORDER BY relation.player.name
        """,
    )
    fun findPlayersByTeamUid(
        @Param("teamUid") teamUid: String,
    ): List<PlayerCore>

    fun findByTeamIdAndPlayerId(
        teamId: Long,
        playerId: Long,
    ): List<TeamPlayerCore>

    fun findByTeamId(teamId: Long): List<TeamPlayerCore>

    fun deleteByTeamIdAndPlayerId(
        teamId: Long,
        playerId: Long,
    )

    fun existsByTeamIdAndPlayerId(
        teamId: Long,
        playerId: Long,
    ): Boolean
}
