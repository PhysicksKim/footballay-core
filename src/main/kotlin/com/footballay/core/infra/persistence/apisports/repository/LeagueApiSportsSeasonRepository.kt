package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LeagueApiSportsSeasonRepository : JpaRepository<LeagueApiSportsSeason, Long> {
    fun findAllByLeagueApiSports(leagueApiSports: LeagueApiSports): List<LeagueApiSportsSeason>
}
