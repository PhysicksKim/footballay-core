package com.footballay.core.infra.apisports.repository

import com.footballay.core.infra.apisports.entity.LeagueApiSports
import com.footballay.core.domain.entity.LeagueCore
import org.springframework.data.jpa.repository.JpaRepository

interface LeagueApiSportsRepository : JpaRepository<LeagueApiSports, Long> {
}