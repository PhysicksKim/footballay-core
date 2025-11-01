package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.TeamPlayerCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamPlayerCoreRepository : JpaRepository<TeamPlayerCore, Long> {
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
