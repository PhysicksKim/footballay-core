package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.TeamCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

import org.springframework.stereotype.Repository

@Repository
interface TeamCoreRepository : JpaRepository<TeamCore, Long> {
} 