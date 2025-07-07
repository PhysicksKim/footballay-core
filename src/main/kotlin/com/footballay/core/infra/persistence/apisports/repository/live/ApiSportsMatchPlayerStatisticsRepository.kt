package com.footballay.core.infra.persistence.apisports.repository.live

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayerStatistics
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import org.springframework.data.jpa.repository.JpaRepository

interface ApiSportsMatchPlayerStatisticsRepository : JpaRepository<ApiSportsMatchPlayerStatistics, String>  {
}