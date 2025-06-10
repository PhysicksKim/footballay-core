package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamApiSportsRepository : JpaRepository<TeamApiSports, Long> {
}