package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.TeamCore
import org.springframework.data.jpa.repository.JpaRepository

interface TeamCoreRepository : JpaRepository<TeamCore, Long> {
} 