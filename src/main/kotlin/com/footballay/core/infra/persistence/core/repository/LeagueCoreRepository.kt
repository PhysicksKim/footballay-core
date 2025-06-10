package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.LeagueCore
import org.springframework.data.jpa.repository.JpaRepository

interface LeagueCoreRepository : JpaRepository<LeagueCore, Long> {
    fun findAllByUidIn(map: List<String>): List<LeagueCore>
    fun findByUid(uid: String): LeagueCore?
} 