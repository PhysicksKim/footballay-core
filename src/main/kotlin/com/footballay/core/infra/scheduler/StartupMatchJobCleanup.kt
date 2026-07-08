package com.footballay.core.infra.scheduler

import com.footballay.core.infra.scheduler.cleanup.MatchJobCleanupResult
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.scheduler.config.StartupMatchJobCleanupProperties
import com.footballay.core.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

data class StartupMatchJobCleanupResult(
    val cleanupResult: MatchJobCleanupResult,
    val reconcileResults: List<ReconcileResult>,
) {
    val success: Boolean
        get() = cleanupResult.success && reconcileResults.all { it.success }
}

/**
 * 이전 Quartz key 규칙으로 남아 있는 available fixture job 을 앱 시작 시 제거하고,
 * DB 의 available fixture 상태를 기준으로 새 key 규칙의 job 을 재생성합니다.
 */
@Component
@ConditionalOnProperty(
    prefix = StartupMatchJobCleanupProperties.PREFIX,
    name = [StartupMatchJobCleanupProperties.ENABLED],
    havingValue = "true",
    matchIfMissing = true,
)
class StartupMatchJobCleanup(
    private val jobSchedulerService: JobSchedulerService,
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val availableFixtureJobReconciler: AvailableFixtureJobReconciler,
) {
    private val log = logger()

    @Order(0)
    @EventListener(ApplicationReadyEvent::class)
    fun cleanupAndReconcileOnStartup() {
        cleanupAndReconcile()
    }

    fun cleanupAndReconcile(): StartupMatchJobCleanupResult {
        val cleanupResult = jobSchedulerService.deleteAvailableMatchJobsForStartupRebuild()
        val reconcileResults =
            when (val loadResult = loadAvailableFixtureLeagueUids()) {
                is AvailableFixtureLeagueUidLoadResult.Success -> loadResult.leagueUids.map(::reconcileLeague)
                is AvailableFixtureLeagueUidLoadResult.Failed -> listOf(loadResult.result)
            }
        val result =
            StartupMatchJobCleanupResult(
                cleanupResult = cleanupResult,
                reconcileResults = reconcileResults,
            )

        if (result.success) {
            log.info(
                "Startup match job cleanup completed - deleted={}, skipped={}, reconciledLeagues={}",
                cleanupResult.deleted,
                cleanupResult.skipped,
                reconcileResults.size,
            )
        } else {
            log.warn(
                "Startup match job cleanup completed with failures - cleanupErrors={}, reconcileFailures={}",
                cleanupResult.errors.size,
                reconcileResults.count { !it.success },
            )
        }

        return result
    }

    private fun loadAvailableFixtureLeagueUids(): AvailableFixtureLeagueUidLoadResult =
        runCatching {
            fixtureCoreRepository.findDistinctLeagueUidsWithAvailableFixtures()
        }.fold(
            onSuccess = { AvailableFixtureLeagueUidLoadResult.Success(it) },
            onFailure = { e ->
                log.error("Failed to load league uids with available fixtures during startup cleanup", e)
                AvailableFixtureLeagueUidLoadResult.Failed(
                    ReconcileResult.empty(fixtureUid = null, leagueUid = null)
                        .copy(
                            success = false,
                            errors =
                                listOf(
                                    ReconcileError(
                                        fixtureUid = null,
                                        leagueUid = null,
                                        phase = null,
                                        operation = "load-available-fixture-league-uids",
                                        message = e.message ?: e::class.simpleName.orEmpty(),
                                    ),
                                ),
                        ),
                )
            },
        )

    private fun reconcileLeague(leagueUid: String): ReconcileResult =
        runCatching {
            availableFixtureJobReconciler.reconcileLeague(leagueUid)
        }.getOrElse { e ->
            log.error("Failed to reconcile available fixture jobs during startup cleanup - leagueUid={}", leagueUid, e)
            ReconcileResult.empty(fixtureUid = null, leagueUid = leagueUid)
                .copy(
                    success = false,
                    errors =
                        listOf(
                            ReconcileError(
                                fixtureUid = null,
                                leagueUid = leagueUid,
                                phase = null,
                                operation = "startup-reconcile-league",
                                message = e.message ?: e::class.simpleName.orEmpty(),
                            ),
                        ),
                )
        }

    private sealed interface AvailableFixtureLeagueUidLoadResult {
        data class Success(
            val leagueUids: List<String>,
        ) : AvailableFixtureLeagueUidLoadResult

        data class Failed(
            val result: ReconcileResult,
        ) : AvailableFixtureLeagueUidLoadResult
    }
}
