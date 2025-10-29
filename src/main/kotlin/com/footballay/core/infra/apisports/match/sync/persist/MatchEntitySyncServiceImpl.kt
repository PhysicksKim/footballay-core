package com.footballay.core.infra.apisports.match.sync.persist

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.FixtureApiSportsDto
import com.footballay.core.infra.apisports.match.sync.dto.LineupSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.PlayerStatSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.TeamStatSyncDto
import com.footballay.core.infra.apisports.match.sync.loader.MatchDataLoader
import com.footballay.core.infra.apisports.match.sync.persist.base.BaseMatchEntitySyncer
import com.footballay.core.infra.apisports.match.sync.persist.event.manager.MatchEventManager
import com.footballay.core.infra.apisports.match.sync.persist.event.manager.MatchEventProcessResult
import com.footballay.core.infra.apisports.match.sync.persist.player.manager.MatchPlayerManager
import com.footballay.core.infra.apisports.match.sync.persist.player.manager.MatchPlayerProcessResult
import com.footballay.core.infra.apisports.match.sync.persist.playerstat.manager.PlayerStatsManager
import com.footballay.core.infra.apisports.match.sync.persist.playerstat.result.PlayerStatsProcessResult
import com.footballay.core.infra.apisports.match.sync.persist.teamstat.manager.TeamStatsManager
import com.footballay.core.infra.apisports.match.sync.persist.teamstat.result.TeamStatsProcessResult
import com.footballay.core.infra.apisports.syncer.match.persist.result.*
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * MatchEntitySyncService 구현체
 * Match Data 관련 Dto 를 받아 DB Entity 로 저장합니다.
 * [MatchPlayerContext] 를 통해 Dto 들에 등장하는 MatchPlayer 들의 정보를 받습니다.
 *
 * ## id=null 선수 처리 중요성
 * ApiSports 는 불안정한 데이터 제공으로 인해 선수 아이디가 누락되는 경우가 빈번합니다.
 * 이 문제는 [MatchPlayerContext] 를 구성하는 쪽에서도 유의하여 동작하는 책임이며,
 * [MatchPlayerContext] 를 사용하는 측과 더불어 DB entity 로 변환하는 과정에서도 유의해야 합니다.
 * [com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer] 를 활용해 id=null 선수를 저장합니다.
 *
 * **처리 단계:**
 * 1. 기존 엔티티 로드 (Fixture, MatchTeam, MatchPlayer, Event)
 * 2. Base DTO 처리 (Fixture + MatchTeam 생성/업데이트)
 * 3. MatchPlayer 처리 (MatchPlayerManager로 통합) + Lineup 정보 적용
 * 4. Event 처리 (생성/업데이트/삭제)
 * 5. PlayerStats 처리 (PlayerStatsManager로 통합)
 * 6. TeamStats 처리 (TeamStatsManager로 통합)
 * 7. 데이터베이스 영속화 (각 Manager에서 완료)
 *
 */
@Service
class MatchEntitySyncServiceImpl(
    private val matchDataLoader: MatchDataLoader,
    private val baseMatchEntitySyncer: BaseMatchEntitySyncer,
    private val matchPlayerManager: MatchPlayerManager,
    private val matchEventManager: MatchEventManager,
    private val playerStatsManager: PlayerStatsManager,
    private val teamStatsManager: TeamStatsManager,
) : MatchEntitySyncService {
    private val log = logger()

    companion object {
        /**
         * Phase별 성능 임계값 (밀리초)
         * 각 Phase의 실행 시간이 이 값을 초과하면 WARN 로그를 출력합니다.
         */
        private const val PHASE1_THRESHOLD_MS = 1000L // Phase1: LoadContext - 기존 엔티티 로드
        private const val PHASE2_THRESHOLD_MS = 1000L // Phase2: BaseEntities - Fixture + MatchTeam 처리
        private const val PHASE3_THRESHOLD_MS = 3000L // Phase3: MatchPlayers - 선수 정보 + Lineup 통합
        private const val PHASE4_THRESHOLD_MS = 3000L // Phase4: MatchEvents - 이벤트 처리
        private const val PHASE5_THRESHOLD_MS = 3000L // Phase5: PlayerStats - 선수 통계 처리
        private const val PHASE6_THRESHOLD_MS = 1000L // Phase6: TeamStats - 팀 통계 처리

        /**
         * 전체 트랜잭션 임계값 (밀리초)
         * 전체 트랜잭션 시간에 따라 로그 레벨을 결정합니다.
         */
        private const val TRANSACTION_WARN_THRESHOLD_MS = 10_000L // 10초 이상: WARN
        private const val TRANSACTION_INFO_THRESHOLD_MS = 5_000L // 5-10초: INFO, 5초 이하: DEBUG

        /**
         * 성능 리포트 관련 설정
         */
        private const val BOTTLENECK_PERCENTAGE_THRESHOLD = 50 // Phase가 전체의 50% 이상 차지하면 병목으로 간주
        private const val PERFORMANCE_BAR_UNIT = 5 // 성능 리포트 바 차트: 5% = 1개의 바(█)
    }

    @Transactional
    override fun syncMatchEntities(
        fixtureApiId: Long,
        baseDto: FixtureApiSportsDto,
        lineupDto: LineupSyncDto,
        eventDto: MatchEventSyncDto,
        teamStatDto: TeamStatSyncDto,
        playerStatDto: PlayerStatSyncDto,
        playerContext: MatchPlayerContext,
    ): MatchEntitySyncResult {
        log.info("Starting entity sync for fixture: $fixtureApiId")
        val transactionStartTime = System.currentTimeMillis()
        val phaseTimings = mutableMapOf<String, Long>()
        val phaseErrors = mutableListOf<String>()

        // Phase 1: 기존 저장된 엔티티들 로드
        val entityBundle = MatchEntityBundle.Companion.createEmpty()
        try {
            val (_, time) =
                measurePhase("Phase1_LoadContext", threshold = PHASE1_THRESHOLD_MS) {
                    matchDataLoader.loadContext(fixtureApiId, playerContext, entityBundle)
                }
            phaseTimings["Phase1_LoadContext"] = time
            log.info(
                "Loaded existing entities - Players: ${entityBundle.allMatchPlayers.size}, Events: ${entityBundle.allEvents.size}",
            )
        } catch (e: Exception) {
            log.error("Phase 1 failed: ${e.message}", e)
            phaseErrors.add("Phase1_LoadContext: ${e.message}")
        }

        // Phase 2: Base DTO 처리 (Fixture + MatchTeam 생성/업데이트)
        try {
            val (result, time) =
                measurePhase("Phase2_BaseEntities", threshold = PHASE2_THRESHOLD_MS) {
                    baseMatchEntitySyncer.syncBaseEntities(fixtureApiId, baseDto, entityBundle)
                }
            phaseTimings["Phase2_BaseEntities"] = time

            if (!result.success) {
                log.error("Base entity sync failed: ${result.errorMessage}")
                phaseErrors.add("Phase2_BaseEntities: ${result.errorMessage}")
                return MatchEntitySyncResult.failure(
                    "failed to sync base entities: ${result.errorMessage}",
                )
            } else {
                log.info(
                    "Base entities synced successfully - Home team: ${result.homeMatchTeam?.teamApiSports?.name}, Away team: ${result.awayMatchTeam?.teamApiSports?.name}",
                )
            }
        } catch (e: Exception) {
            log.error("Phase 2 failed: ${e.message}", e)
            phaseErrors.add("Phase2_BaseEntities: ${e.message}")
            return MatchEntitySyncResult.failure(
                "failed to sync base entities: ${e.message}",
            )
        }

        // Phase 3: MatchPlayer 처리 + Lineup 정보 적용 (MatchPlayerManager로 통합)
        val matchPlayerResult =
            try {
                val (result, time) =
                    measurePhase("Phase3_MatchPlayers", threshold = PHASE3_THRESHOLD_MS) {
                        matchPlayerManager.processMatchTeamAndPlayers(playerContext, lineupDto, entityBundle)
                    }
                phaseTimings["Phase3_MatchPlayers"] = time
                log.info(
                    "MatchPlayer processing completed - Total players: ${result.totalPlayers}, Created: ${result.createdCount}, Updated: ${result.updatedCount}, Deleted: ${result.deletedCount}",
                )
                result
            } catch (e: Exception) {
                log.error("Phase 3 failed: ${e.message}", e)
                phaseErrors.add("Phase3_MatchPlayers: ${e.message}")
                MatchPlayerProcessResult.empty()

                // 라인업 저장 에러시에는 이후 진행하기 어려우므로 실패 처리.
                return MatchEntitySyncResult.failure(
                    "failed to process Match Lineup: ${e.message}",
                )
            }

        // Phase 4: Event 처리 (MatchEventManager로 통합)
        val matchEventResult =
            try {
                val (result, time) =
                    measurePhase("Phase4_MatchEvents", threshold = PHASE4_THRESHOLD_MS) {
                        matchEventManager.processMatchEvents(eventDto, entityBundle)
                    }
                phaseTimings["Phase4_MatchEvents"] = time
                log.info(
                    "MatchEvent processing completed - Total events: ${result.totalEvents}, Created: ${result.createdCount}, Updated: ${result.updatedCount}, Deleted: ${result.deletedCount}",
                )
                result
            } catch (e: Exception) {
                log.error("Phase 4 failed: ${e.message}", e)
                phaseErrors.add("Phase4_MatchEvents: ${e.message}")
                MatchEventProcessResult(0, 0, 0, 0, emptyList())
            }

        // Phase 5: PlayerStats 처리 (PlayerStatsManager로 통합)
        val playerStatsResult =
            try {
                val (result, time) =
                    measurePhase("Phase5_PlayerStats", threshold = PHASE5_THRESHOLD_MS) {
                        playerStatsManager.processPlayerStats(playerStatDto, entityBundle)
                    }
                phaseTimings["Phase5_PlayerStats"] = time
                log.info(
                    "PlayerStats processing completed - Total stats: ${result.totalStats}, Created: ${result.createdCount}, Updated: ${result.updatedCount}, Deleted: ${result.deletedCount}",
                )
                result
            } catch (e: Exception) {
                log.error("Phase 5 failed: ${e.message}", e)
                phaseErrors.add("Phase5_PlayerStats: ${e.message}")
                PlayerStatsProcessResult(0, 0, 0, 0, emptyList())
            }

        // Phase 6: TeamStats 처리 (TeamStatsManager로 통합)
        val teamStatsResult =
            try {
                val (result, time) =
                    measurePhase("Phase6_TeamStats", threshold = PHASE6_THRESHOLD_MS) {
                        teamStatsManager.processTeamStats(teamStatDto, entityBundle)
                    }
                phaseTimings["Phase6_TeamStats"] = time
                log.info(
                    "TeamStats processing completed - Home: ${result.hasHome}, Away: ${result.hasAway}, Created: ${result.createdCount}, Updated: ${result.updatedCount}",
                )
                result
            } catch (e: Exception) {
                log.error("Phase 6 failed: ${e.message}", e)
                phaseErrors.add("Phase6_TeamStats: ${e.message}")
                TeamStatsProcessResult(false, false, 0, 0, null, null)
            }

        // 전체 트랜잭션 시간 측정 및 성능 리포트
        val totalTransactionTime = System.currentTimeMillis() - transactionStartTime
        logPerformanceReport(fixtureApiId, phaseTimings, totalTransactionTime)

        // 에러가 있었는지 로깅
        if (phaseErrors.isNotEmpty()) {
            log.warn(
                "Entity sync completed with ${phaseErrors.size} phase error(s) for fixture: $fixtureApiId - Errors: ${phaseErrors.joinToString(
                    "; ",
                )}",
            )
        } else {
            log.info("All entities persisted successfully for fixture: $fixtureApiId")
        }

        return MatchEntitySyncResult.success(
            createdCount =
                matchPlayerResult.createdCount + matchEventResult.createdCount + playerStatsResult.createdCount +
                    teamStatsResult.createdCount,
            updatedCount =
                matchPlayerResult.updatedCount + matchEventResult.updatedCount + playerStatsResult.updatedCount +
                    teamStatsResult.updatedCount,
            deletedCount =
                matchPlayerResult.deletedCount + matchEventResult.deletedCount + playerStatsResult.deletedCount,
            playerChanges =
                MatchPlayerSyncResult(
                    created = matchPlayerResult.createdCount,
                    updated = matchPlayerResult.updatedCount,
                    deleted = matchPlayerResult.deletedCount,
                ),
            eventChanges =
                MatchEventSyncResult(
                    created = matchEventResult.createdCount,
                    updated = matchEventResult.updatedCount,
                    deleted = matchEventResult.deletedCount,
                ),
        )
    }

    /**
     * Phase별 성능 측정 헬퍼
     *
     * **프로덕션 성능 모니터링:**
     * - 각 Phase의 실행 시간을 측정하고 로그로 출력
     * - 느린 Phase는 WARN 레벨로 경고
     * - 운영 중 성능 병목 지점 파악 가능
     *
     * **임계값 (companion object 상수 참조):**
     * - Phase 1-2: 1초 (PHASE1_THRESHOLD_MS, PHASE2_THRESHOLD_MS)
     * - Phase 3-5: 3초 (PHASE3~5_THRESHOLD_MS)
     * - Phase 6: 1초 (PHASE6_THRESHOLD_MS)
     *
     * @param phaseName Phase 이름 (예: "Phase1_Load")
     * @param threshold 경고 임계값 (밀리초)
     * @param block 측정할 코드 블록
     * @return 실행 결과와 경과 시간의 Pair
     */
    private fun <T> measurePhase(
        phaseName: String,
        threshold: Long,
        block: () -> T,
    ): Pair<T, Long> {
        val startTime = System.currentTimeMillis()
        val result =
            try {
                block()
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - startTime
                log.error("[$phaseName] failed after ${elapsed}ms", e)
                throw e
            }

        val elapsed = System.currentTimeMillis() - startTime

        // Phase 완료 로그 (항상 INFO 레벨로 출력하여 테스트에서도 확인 가능)
        if (elapsed > threshold) {
            log.warn("[$phaseName] took too long: ${elapsed}ms (threshold: ${threshold}ms)")
        } else {
            log.info("[$phaseName] completed in ${elapsed}ms")
        }

        return Pair(result, elapsed)
    }

    /**
     * 성능 리포트 로깅
     *
     * **프로덕션 모니터링:**
     * - 전체 트랜잭션 시간
     * - 각 Phase별 시간 및 비율
     * - 가장 느린 Phase 표시
     * - 전체 시간이 임계값 초과 시 WARN
     *
     * **로그 레벨:**
     * - 전체 10초 이상: WARN (심각한 성능 저하)
     * - 전체 10초 이하: INFO (정상 범위)
     *
     * **테스트 환경:**
     * - INFO 레벨로 항상 출력되므로 테스트에서도 확인 가능
     */
    private fun logPerformanceReport(
        fixtureApiId: Long,
        phaseTimings: Map<String, Long>,
        totalTime: Long,
    ) {
        val sortedPhases = phaseTimings.entries.sortedByDescending { it.value }
        val slowestPhase = sortedPhases.firstOrNull()

        val report =
            buildString {
                appendLine("========================================")
                appendLine("Performance Report - Fixture: $fixtureApiId")
                appendLine("========================================")
                appendLine("Total Transaction Time: ${totalTime}ms")
                appendLine("----------------------------------------")

                sortedPhases.forEach { (phase, time) ->
                    val percentage = (time.toDouble() / totalTime * 100).toInt()
                    val bar = "█".repeat(percentage / PERFORMANCE_BAR_UNIT)
                    appendLine(String.format("%-20s: %5dms (%3d%%) %s", phase, time, percentage, bar))
                }

                appendLine("----------------------------------------")
                slowestPhase?.let { (phase, time) ->
                    val percentage = (time.toDouble() / totalTime * 100).toInt()
                    appendLine("Slowest Phase: $phase (${time}ms, $percentage%)")
                }
                appendLine("========================================")
            }

        // 성능 리포트는 항상 INFO 레벨로 출력 (테스트/운영 모두 중요한 정보)
        // 단, 임계값 초과 시에는 WARN으로 강조
        if (totalTime > TRANSACTION_WARN_THRESHOLD_MS) {
            log.warn(report)
        } else {
            log.info(report)
        }

        // 추가 경고: 특정 Phase가 전체의 일정 비율 이상 차지하는 경우
        slowestPhase?.let { (phase, time) ->
            val percentage = (time.toDouble() / totalTime * 100).toInt()
            if (percentage > BOTTLENECK_PERCENTAGE_THRESHOLD) {
                log.warn("BOTTLENECK DETECTED: $phase takes $percentage% of total time (${time}ms / ${totalTime}ms)")
            }
        }
    }
}
