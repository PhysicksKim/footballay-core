package com.footballay.core.infra.apisports.match.sync.persist.teamstat.manager

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.dto.MatchTeamStatisticsDto
import com.footballay.core.infra.apisports.match.sync.dto.TeamStatSyncDto
import com.footballay.core.infra.apisports.match.sync.persist.teamstat.result.TeamStatsProcessResult
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeamStatistics
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeamXG
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchTeamStatisticsRepository
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * TeamStats 통합 관리자
 *
 * TeamStats의 생성, 업데이트, 저장을 통합하여 관리합니다.
 *
 * **처리 과정:**
 * 1. Home/Away TeamStats 각각 처리
 * 2. XG 리스트 처리 (시간별 기대득점)
 * 3. MatchTeam과 양방향 연관관계 설정
 * 4. 영속 상태 저장
 * 5. EntityBundle 업데이트
 *
 * **특징:**
 * - PlayerStats와 달리 복잡한 Collector/Planner 불필요
 * - Home/Away 2개만 처리하므로 단순한 구조
 * - MatchTeam은 Phase 2에서 이미 생성되어 EntityBundle에 존재
 * - 배치 처리로 성능 최적화
 *
 * **중요한 비즈니스 로직:**
 * - Home/Away 각각 DTO가 null이면 처리 스킵
 * - XG 리스트는 elapsed time 기준으로 업데이트
 * - MatchTeam과 1:1 관계 보장
 */
@Component
class TeamStatsManager(
    private val teamStatsRepository: ApiSportsMatchTeamStatisticsRepository
) {

    private val log = logger()

    /**
     * TeamStats를 MatchTeam과 연결하여 생성/업데이트하고 영속 상태로 만듭니다.
     *
     * **핵심 로직:**
     * 1. Home TeamStats 처리 (DTO 있으면)
     * 2. Away TeamStats 처리 (DTO 있으면)
     * 3. 배치 저장
     * 4. EntityBundle 업데이트
     *
     * **중요한 제약사항:**
     * - MatchTeam은 Phase 2에서 이미 영속화됨
     * - EntityBundle에서 MatchTeam 조회 (Repository 사용 금지)
     * - Home/Away DTO가 null이면 해당 TeamStats 처리 안 함
     *
     * @param teamStatDto TeamStats 정보 DTO
     * @param entityBundle 기존 엔티티 번들 (업데이트됨)
     * @return TeamStats 처리 결과
     */
    @Transactional
    fun processTeamStats(
        teamStatDto: TeamStatSyncDto,
        entityBundle: MatchEntityBundle
    ): TeamStatsProcessResult {
        log.info("Starting TeamStats processing - Home: ${teamStatDto.homeStats != null}, Away: ${teamStatDto.awayStats != null}")

        try {
            var createdCount = 0
            var updatedCount = 0

            // 1. Home TeamStats 처리
            val homeTeamStat = teamStatDto.homeStats?.let { dto ->
                processTeamStat(dto, entityBundle.homeTeam, entityBundle.homeTeamStat).also {
                    if (entityBundle.homeTeamStat == null) createdCount++ else updatedCount++
                }
            }

            // 2. Away TeamStats 처리
            val awayTeamStat = teamStatDto.awayStats?.let { dto ->
                processTeamStat(dto, entityBundle.awayTeam, entityBundle.awayTeamStat).also {
                    if (entityBundle.awayTeamStat == null) createdCount++ else updatedCount++
                }
            }

            // 3. 배치 저장
            val statsToSave = listOfNotNull(homeTeamStat, awayTeamStat)
            if (statsToSave.isNotEmpty()) {
                teamStatsRepository.saveAll(statsToSave)
                log.info("Saved ${statsToSave.size} TeamStats entities")
            }

            // 4. EntityBundle 업데이트
            entityBundle.homeTeamStat = homeTeamStat
            entityBundle.awayTeamStat = awayTeamStat

            log.info("TeamStats processing completed - Home: ${homeTeamStat != null}, Away: ${awayTeamStat != null}, Created: $createdCount, Updated: $updatedCount")

            return TeamStatsProcessResult(
                hasHome = homeTeamStat != null,
                hasAway = awayTeamStat != null,
                createdCount = createdCount,
                updatedCount = updatedCount,
                homeTeamStat = homeTeamStat,
                awayTeamStat = awayTeamStat
            )

        } catch (e: Exception) {
            log.error("Failed to process TeamStats", e)
            throw e
        }
    }

    /**
     * 개별 TeamStat 처리 (Home 또는 Away)
     *
     * **로직:**
     * 1. 기존 TeamStat 있으면 업데이트, 없으면 생성
     * 2. DTO의 모든 필드를 엔티티에 반영
     * 3. XG 리스트 처리
     * 4. MatchTeam과 양방향 연관관계 설정
     *
     * @param dto TeamStats DTO
     * @param matchTeam 연관된 MatchTeam (Phase 2에서 생성됨)
     * @param existingStat 기존 TeamStats (있으면)
     * @return 생성/업데이트된 TeamStats
     */
    private fun processTeamStat(
        dto: MatchTeamStatisticsDto,
        matchTeam: ApiSportsMatchTeam?,
        existingStat: ApiSportsMatchTeamStatistics?
    ): ApiSportsMatchTeamStatistics {
        if (matchTeam == null) {
            throw IllegalStateException("MatchTeam must not be null. It should be created in Phase 2.")
        }

        // 기존 있으면 업데이트, 없으면 생성
        val teamStat = existingStat ?: ApiSportsMatchTeamStatistics(
            matchTeam = matchTeam,
            xgList = mutableListOf()
        )

        // 필드 업데이트
        updateTeamStatFields(teamStat, dto)

        // XG 리스트 처리
        processXGList(teamStat, dto.xgList)

        // 양방향 연관관계 설정
        teamStat.matchTeam = matchTeam
        matchTeam.teamStatistics = teamStat

        return teamStat
    }

    /**
     * TeamStats 필드 업데이트
     *
     * DTO의 모든 통계 필드를 엔티티에 반영합니다.
     */
    private fun updateTeamStatFields(
        teamStat: ApiSportsMatchTeamStatistics,
        dto: MatchTeamStatisticsDto
    ) {
        teamStat.apply {
            // 슈팅 관련 통계
            shotsOnGoal = dto.shotsOnGoal
            shotsOffGoal = dto.shotsOffGoal
            totalShots = dto.totalShots
            blockedShots = dto.blockedShots
            shotsInsideBox = dto.shotsInsideBox
            shotsOutsideBox = dto.shotsOutsideBox

            // 기타 경기 통계
            fouls = dto.fouls
            cornerKicks = dto.cornerKicks
            offsides = dto.offsides
            ballPossession = dto.ballPossession

            // 카드 관련 통계
            yellowCards = dto.yellowCards
            redCards = dto.redCards

            // 골키퍼 관련 통계
            goalkeeperSaves = dto.goalkeeperSaves

            // 패스 관련 통계
            totalPasses = dto.totalPasses
            passesAccurate = dto.passesAccurate
            passesPercentage = dto.passesPercentage

            // 기대득점 관련 통계
            goalsPrevented = dto.goalsPrevented
        }
    }

    /**
     * XG 리스트 처리
     *
     * **로직:**
     * - 기존 XG를 elapsed time으로 매핑
     * - DTO의 XG와 비교하여 생성/업데이트
     * - UniqueConstraint: (match_team_statistics_id, elapsed_time)
     *
     * **중요:**
     * - XG는 시간별로 변하는 값
     * - 같은 elapsed time이면 업데이트
     * - 새로운 elapsed time이면 추가
     *
     * @param teamStat 대상 TeamStats 엔티티
     * @param xgDtoList XG DTO 리스트
     */
    private fun processXGList(
        teamStat: ApiSportsMatchTeamStatistics,
        xgDtoList: List<MatchTeamStatisticsDto.MatchTeamXGDto>
    ) {
        if (xgDtoList.isEmpty()) {
            log.debug("No XG data to process")
            return
        }

        // 기존 XG 맵 (elapsed time → entity)
        val existingXgMap = teamStat.xgList.associateBy { it.elapsedTime }.toMutableMap()

        // DTO에서 온 XG 처리
        xgDtoList.forEach { dto ->
            val existing = existingXgMap[dto.elapsed]
            if (existing != null) {
                // 업데이트
                existing.expectedGoals = dto.xg
                log.debug("Updated XG at elapsed ${dto.elapsed}: ${dto.xg}")
            } else {
                // 생성
                val newXg = ApiSportsMatchTeamXG(
                    matchTeamStatistics = teamStat,
                    elapsedTime = dto.elapsed,
                    expectedGoals = dto.xg
                )
                teamStat.xgList.add(newXg)
                log.debug("Created new XG at elapsed ${dto.elapsed}: ${dto.xg}")
            }
        }

        log.info("Processed ${xgDtoList.size} XG entries for TeamStats")
    }
}
