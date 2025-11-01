package com.footballay.core.infra.persistence.apisports.repository.live

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeamStatistics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ApiSportsMatchTeamStatisticsRepository : JpaRepository<ApiSportsMatchTeamStatistics, Long>
