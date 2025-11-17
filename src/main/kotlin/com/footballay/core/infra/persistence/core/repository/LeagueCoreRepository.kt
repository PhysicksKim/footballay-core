package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.LeagueCore
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LeagueCoreRepository : JpaRepository<LeagueCore, Long> {
    @EntityGraph(attributePaths = ["apiSportsLeague"])
    fun findByAvailableTrue(): List<LeagueCore>

    fun findByUid(uid: String): LeagueCore?
}
