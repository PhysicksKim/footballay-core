package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.FixtureCore
import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.stereotype.Repository

@Repository
interface FixtureCoreRepository : JpaRepository<FixtureCore, Long> {
} 