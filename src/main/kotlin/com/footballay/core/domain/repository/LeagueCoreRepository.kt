package com.footballay.core.domain.repository

import com.footballay.core.domain.entity.LeagueCore
import org.springframework.data.jpa.repository.JpaRepository

interface LeagueCoreRepository : JpaRepository<LeagueCore, Long> {
} 