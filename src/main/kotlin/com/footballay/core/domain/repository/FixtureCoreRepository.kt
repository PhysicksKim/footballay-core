package com.footballay.core.domain.repository

import com.footballay.core.domain.entity.FixtureCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface FixtureCoreRepository : JpaRepository<FixtureCore, Long> {
} 