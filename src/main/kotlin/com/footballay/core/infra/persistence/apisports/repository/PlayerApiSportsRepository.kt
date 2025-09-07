package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PlayerApiSportsRepository : JpaRepository<PlayerApiSports, Long> {

    fun findByApiId(apiId: Long): PlayerApiSports?

    fun findAllByApiIdIn(apiIds: List<Long>): List<PlayerApiSports>
    
    @Query("SELECT p FROM PlayerApiSports p LEFT JOIN FETCH p.playerCore WHERE p.apiId = :apiId")
    fun findPlayerApiSportsByApiIdWithPlayerCore(apiId: Long): PlayerApiSports?
    
    /**
     * N+1 문제 해결을 위한 일괄 조회 메서드
     * 여러 apiId에 대해 PlayerApiSports와 PlayerCore를 함께 조회
     */
    @Query("SELECT p FROM PlayerApiSports p LEFT JOIN FETCH p.playerCore WHERE p.apiId IN :apiIds")
    fun findPlayerApiSportsByApiIdsWithPlayerCore(@Param("apiIds") apiIds: List<Long>): List<PlayerApiSports>
    
    /**
     * PlayerCore ID로 PlayerApiSports를 찾는 메서드
     */
    fun findByPlayerCoreId(playerCoreId: Long): PlayerApiSports?
}