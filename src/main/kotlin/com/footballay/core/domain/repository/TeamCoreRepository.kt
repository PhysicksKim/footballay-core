package com.footballay.core.domain.repository

import com.footballay.core.domain.entity.TeamCore
import org.springframework.data.jpa.repository.JpaRepository

interface TeamCoreRepository : JpaRepository<TeamCore, Long> {
} 