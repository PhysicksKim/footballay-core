package com.footballay.core.infra.apisports.repository

import com.footballay.core.infra.apisports.entity.FixtureApiSports
import com.footballay.core.domain.entity.FixtureCore
import org.springframework.data.jpa.repository.JpaRepository

interface FixtureApiSportsRepository : JpaRepository<FixtureApiSports, Long> {
}