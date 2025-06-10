package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.PlayerCore
import org.springframework.data.jpa.repository.JpaRepository

interface PlayerCoreRepository : JpaRepository<PlayerCore, Long> {
} 