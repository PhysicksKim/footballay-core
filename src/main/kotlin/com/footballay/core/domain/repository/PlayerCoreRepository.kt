package com.footballay.core.domain.repository

import com.footballay.core.domain.entity.PlayerCore
import org.springframework.data.jpa.repository.JpaRepository

interface PlayerCoreRepository : JpaRepository<PlayerCore, Long> {
} 