package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueSeasonCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LeagueSeasonCoreRepository : JpaRepository<LeagueSeasonCore, Long> {
    fun findByLeagueAndSeasonYear(
        league: LeagueCore,
        seasonYear: Int,
    ): LeagueSeasonCore?

    fun findByLeagueAndCurrentTrue(league: LeagueCore): List<LeagueSeasonCore>
}
