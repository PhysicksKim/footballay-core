package com.footballay.core.infra.apisports.match.sync.teamstat

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchTeamStatisticsDto
import com.footballay.core.infra.apisports.match.sync.dto.TeamStatSyncDto
import com.footballay.core.logger
import org.springframework.stereotype.Component

/**
 * 팀 통계 데이터를 동기화합니다.
 * 
 * 책임:
 * - FullMatchSyncDto에서 팀별 경기 통계 데이터 추출
 * - 점유율, 슈팅, 패스 등 팀 단위 통계 정보 변환
 * - 현재 시점의 xG 값만 포함 (시계열 처리는 엔티티 저장 시 처리)
 */
@Component
class TeamStatSyncer : MatchTeamStatDtoExtractor {

    private val log = logger()

    override fun extractTeamStats(dto: FullMatchSyncDto): TeamStatSyncDto {
        log.info("Starting team stats sync for fixture: ${dto.fixture.id}")
        
        // 통계 데이터 개수 검증 - 0개(정상: 아직 집계 안됨) 또는 2개(정상: 양팀 통계)만 허용
        when (dto.statistics.size) {
            0 -> {
                log.debug("No statistics available yet for fixture: ${dto.fixture.id}")
                return TeamStatSyncDto.Companion.empty()
            }
            2 -> {
                // 정상 케이스 - 계속 진행
            }
            else -> {
                log.warn("Expected 0 or 2 team statistics but got ${dto.statistics.size} for fixture: ${dto.fixture.id}. " +
                        "This may indicate incomplete data aggregation or data corruption.")
                return TeamStatSyncDto.Companion.empty()
            }
        }
        
        // 팀 ID 매칭
        val homeStats = dto.statistics.find { it.team.id == dto.teams.home.id }
        val awayStats = dto.statistics.find { it.team.id == dto.teams.away.id }
        if(homeStats == null || awayStats == null) {
            log.warn("home/away team 아이디에 일치하는 통계 팀을 찾을 수 없습니다. fixtureApiId=${dto.fixture.id}, Home:${homeStats?.team}, Away:${awayStats?.team}")
            return TeamStatSyncDto.Companion.empty()
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
        val currentXG = extractValidXG(teamStatsDto.statistics.expectedGoals, currentElapsed, teamApiId)
        
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

    /**
     * 유효한 xG 값을 추출합니다.
     * 
     * **검증 규칙:**
     * - elapsed time이 null이면 빈 리스트 반환 (이전 데이터 유지)
     * - xG 값이 null/빈 문자열이면 빈 리스트 반환
     * - xG 파싱 실패 시 빈 리스트 반환 (이전 데이터 유지)
     * - xG가 음수면 필터링 (빈 리스트 반환)
     * 
     * **중요:** 빈 리스트 반환 시 TeamStatsManager에서 기존 XG 데이터를 유지합니다.
     * 
     * @param expectedGoalsStr xG 문자열 값
     * @param currentElapsed 현재 경기 시간 (분)
     * @param teamApiId 로깅용 팀 ID
     * @return 유효한 xG 리스트 또는 빈 리스트
     */
    private fun extractValidXG(
        expectedGoalsStr: String?,
        currentElapsed: Int?,
        teamApiId: Long
    ): List<MatchTeamStatisticsDto.MatchTeamXGDto> {
        // elapsed time 없으면 xG 기록 불가
        if (currentElapsed == null) {
            log.debug("No elapsed time available, skipping XG extraction for team: $teamApiId")
            return emptyList()
        }
        
        // xG 값이 없으면 빈 리스트 (기존 데이터 유지)
        if (expectedGoalsStr.isNullOrBlank()) {
            log.debug("No XG value provided for team: $teamApiId at elapsed: $currentElapsed")
            return emptyList()
        }
        
        // xG 파싱 시도
        val xgValue = try {
            expectedGoalsStr.toDouble()
        } catch (e: NumberFormatException) {
            log.warn("Invalid XG format for team $teamApiId: '$expectedGoalsStr' at elapsed $currentElapsed. Skipping XG update to preserve existing data.")
            return emptyList()
        }
        
        // 음수 검증
        if (xgValue < 0.0) {
            log.warn("Negative XG value detected for team $teamApiId: $xgValue at elapsed $currentElapsed. Filtering out invalid value.")
            return emptyList()
        }
        
        // 유효한 xG 값
        return listOf(
            MatchTeamStatisticsDto.MatchTeamXGDto(
                xg = xgValue,
                elapsed = currentElapsed
            )
        )
    }
} 