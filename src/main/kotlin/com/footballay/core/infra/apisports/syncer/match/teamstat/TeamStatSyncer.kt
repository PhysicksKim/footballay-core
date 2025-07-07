package com.footballay.core.infra.apisports.syncer.match.teamstat

import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.syncer.match.dto.TeamStatSyncDto
import com.footballay.core.infra.apisports.syncer.match.dto.MatchTeamStatisticsDto
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * 팀 통계 데이터를 동기화합니다.
 * 
 * 책임:
 * - FullMatchSyncDto에서 팀별 경기 통계 데이터 추출
 * - 점유율, 슈팅, 패스 등 팀 단위 통계 정보 변환
 * - 현재 시점의 xG 값만 포함 (시계열 처리는 엔티티 저장 시 처리)
 */
@Component
class TeamStatSyncer : MatchTeamStatSync {

    private val log = logger()

    override fun syncTeamStats(dto: FullMatchSyncDto): TeamStatSyncDto {
        log.info("Starting team stats sync for fixture: ${dto.fixture.id}")
        if(dto.statistics.size < 2) {
            return TeamStatSyncDto.empty()
        }
        
        val homeStats = dto.statistics.find { it.team.id == dto.teams.home.id }
        val awayStats = dto.statistics.find { it.team.id == dto.teams.away.id }
        if(homeStats == null || awayStats == null) {
            log.warn("home/away team 아이디에 일치하는 통계 팀을 찾을 수 없습니다. fixtureApiId=${dto.fixture.id}, Home:${homeStats?.team}, Away:${awayStats?.team}")
            return TeamStatSyncDto.empty()
        }
        
        return TeamStatSyncDto(
            homeStats = extractTeamStatistics(homeStats, dto.fixture.status.elapsed),
            awayStats = extractTeamStatistics(awayStats, dto.fixture.status.elapsed)
        )
    }

    /**
     * 개별 팀 통계를 추출합니다.
     * 
     * @param teamStatsDto 팀 통계 DTO
     * @param currentElapsed 현재 경기 시간 (분)
     * @return 추출된 팀 통계 DTO
     */
    private fun extractTeamStatistics(
        teamStatsDto: FullMatchSyncDto.TeamStatisticsDto, 
        currentElapsed: Int?
    ): MatchTeamStatisticsDto {
        val teamApiId = teamStatsDto.team.id ?: throw IllegalArgumentException("Team ID cannot be null")
        
        // 현재 시점의 xG 값만 포함 (시계열 처리는 엔티티 저장 시 처리)
        val currentXG = if (currentElapsed != null && teamStatsDto.statistics.expectedGoals != null) {
            listOf(
                MatchTeamStatisticsDto.MatchTeamXGDto(
                    xg = try{teamStatsDto.statistics.expectedGoals.toDouble()} catch (e: NumberFormatException) {
                        log.error("Invalid expectedGoals value: ${teamStatsDto.statistics.expectedGoals}", e)
                        0.0
                    },
                    elapsed = currentElapsed
                )
            )
        } else {
            emptyList()
        }
        
        return MatchTeamStatisticsDto(
            teamApiId = teamApiId,
            // 슈팅 관련 통계
            shotsOnGoal = teamStatsDto.statistics.shotsOnGoal,
            shotsOffGoal = teamStatsDto.statistics.shotsOffGoal,
            totalShots = teamStatsDto.statistics.totalShots,
            blockedShots = teamStatsDto.statistics.blockedShots,
            shotsInsideBox = teamStatsDto.statistics.shotsInsideBox,
            shotsOutsideBox = teamStatsDto.statistics.shotsOutsideBox,
            // 기타 경기 통계
            fouls = teamStatsDto.statistics.fouls,
            cornerKicks = teamStatsDto.statistics.cornerKicks,
            offsides = teamStatsDto.statistics.offsides,
            ballPossession = teamStatsDto.statistics.ballPossession,
            // 카드 관련 통계
            yellowCards = teamStatsDto.statistics.yellowCards,
            redCards = teamStatsDto.statistics.redCards,
            // 골키퍼 관련 통계
            goalkeeperSaves = teamStatsDto.statistics.goalkeeperSaves,
            // 패스 관련 통계
            totalPasses = teamStatsDto.statistics.totalPasses,
            passesAccurate = teamStatsDto.statistics.passesAccurate,
            passesPercentage = teamStatsDto.statistics.passesPercentage,
            // 기대득점 관련 통계
            goalsPrevented = teamStatsDto.statistics.goalsPrevented,
            // 현재 시점의 xG 값만 포함
            xgList = currentXG
        )
    }
} 