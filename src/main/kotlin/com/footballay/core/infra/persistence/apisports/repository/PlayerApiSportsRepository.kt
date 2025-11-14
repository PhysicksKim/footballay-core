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
    fun findPlayerApiSportsByApiIdsWithPlayerCore(
        @Param("apiIds") apiIds: List<Long>,
    ): List<PlayerApiSports>

    /**
     * PlayerCore ID로 PlayerApiSports를 찾는 메서드
     */
    fun findByPlayerCoreId(playerCoreId: Long): PlayerApiSports?

    /**
     * TeamApiSports apiId로 해당 팀의 PlayerApiSports 목록 조회
     *
     * TeamApiSports → TeamCore → TeamPlayerCore → PlayerCore → PlayerApiSports 순서로 조인하여 조회합니다.
     * Admin frontend에서 팀별 선수 목록을 조회할 때 사용됩니다.
     *
     * @param teamApiId TeamApiSports의 apiId
     * @return 해당 팀에 속한 PlayerApiSports 목록 (PlayerCore와 함께 fetch)
     */
    @Query(
        """
        SELECT DISTINCT pas FROM PlayerApiSports pas
        LEFT JOIN FETCH pas.playerCore pc
        JOIN TeamPlayerCore tpc ON tpc.player = pc
        JOIN tpc.team tc
        JOIN TeamApiSports tas ON tas.teamCore = tc
        WHERE tas.apiId = :teamApiId
    """,
    )
    fun findAllByTeamApiSportsApiId(
        @Param("teamApiId") teamApiId: Long,
    ): List<PlayerApiSports>
}
