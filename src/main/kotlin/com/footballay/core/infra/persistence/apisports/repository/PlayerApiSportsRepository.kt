package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PlayerApiSportsRepository : JpaRepository<PlayerApiSports, Long> {
    fun findAllByApiIdIn(apiIds: List<Long>): List<PlayerApiSports>
    
    @Query("SELECT p FROM PlayerApiSports p LEFT JOIN FETCH p.playerCore WHERE p.apiId = :apiId")
    fun findPlayerApiSportsByApiIdWithPlayerCore(apiId: Long): PlayerApiSports?
}