package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FixtureApiSportsRepository : JpaRepository<FixtureApiSports, Long> {
}