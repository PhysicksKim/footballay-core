package com.footballay.core.infra.persistence.apisports.repository.live

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import org.springframework.data.jpa.repository.JpaRepository

interface ApiSportsMatchPlayerRepository : JpaRepository<ApiSportsMatchPlayer, String>
