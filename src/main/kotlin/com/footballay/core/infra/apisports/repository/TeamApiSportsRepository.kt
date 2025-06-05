package com.footballay.core.infra.apisports.repository

import com.footballay.core.infra.apisports.entity.TeamApiSports
import com.footballay.core.domain.entity.TeamCore
import org.springframework.data.jpa.repository.JpaRepository

interface TeamApiSportsRepository : JpaRepository<TeamApiSports, Long> {
    fun findByTeamCore(teamCore: TeamCore): TeamApiSports?
    fun findByApiId(apiId: Long): TeamApiSports?
} 