package com.footballay.core.infra.persistence.apisports.repository.live

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeamXG
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ApiSportsMatchTeamXGRepository : JpaRepository<ApiSportsMatchTeamXG, Long> {
    /**
     * 특정 팀 통계의 xG 기록을 elapsed time 순으로 조회
     */
    @Query(
        """
        SELECT xg FROM ApiSportsMatchTeamXG xg 
        WHERE xg.matchTeamStatistics.id = :teamStatisticsId 
        ORDER BY xg.elapsedTime ASC
    """,
    )
    fun findByTeamStatisticsIdOrderByElapsedTime(
        @Param("teamStatisticsId") teamStatisticsId: Long,
    ): List<ApiSportsMatchTeamXG>

    /**
     * 특정 팀 통계의 가장 최근 xG 기록 조회
     */
    @Query(
        """
        SELECT xg FROM ApiSportsMatchTeamXG xg 
        WHERE xg.matchTeamStatistics.id = :teamStatisticsId 
        ORDER BY xg.elapsedTime DESC 
        LIMIT 1
    """,
    )
    fun findLatestByTeamStatisticsId(
        @Param("teamStatisticsId") teamStatisticsId: Long,
    ): ApiSportsMatchTeamXG?

    /**
     * 특정 팀 통계의 특정 elapsed time의 xG 기록 조회
     */
    fun findByMatchTeamStatisticsIdAndElapsedTime(
        teamStatisticsId: Long,
        elapsedTime: Int,
    ): ApiSportsMatchTeamXG?
}
