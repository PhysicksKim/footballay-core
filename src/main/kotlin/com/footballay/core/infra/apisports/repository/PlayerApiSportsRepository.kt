package com.footballay.core.infra.apisports.repository

import com.footballay.core.infra.apisports.entity.PlayerApiSports
import com.footballay.core.domain.entity.PlayerCore
import org.springframework.data.jpa.repository.JpaRepository

interface PlayerApiSportsRepository : JpaRepository<PlayerApiSports, Long> {
}