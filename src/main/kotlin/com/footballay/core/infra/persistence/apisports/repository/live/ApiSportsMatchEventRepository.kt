package com.footballay.core.infra.persistence.apisports.repository.live

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import org.springframework.data.jpa.repository.JpaRepository

interface ApiSportsMatchEventRepository : JpaRepository<ApiSportsMatchEvent, String> {
}