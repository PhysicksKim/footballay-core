package com.footballay.core.infra.scheduler

import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class StartupMatchJobCleanupTest {
    @Mock
    private lateinit var jobSchedulerService: JobSchedulerService

    @Mock
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Mock
    private lateinit var availableFixtureJobReconciler: AvailableFixtureJobReconciler

    @Test
    fun `startup cleanup은 old job 삭제 후 available fixture가 있는 리그를 reconcile한다`() {
        val service = service()
        whenever(jobSchedulerService.deleteStartupAvailableMatchJobs())
            .thenReturn(MatchJobCleanupResult(deleted = 2, skipped = 1, errors = emptyList()))
        whenever(fixtureCoreRepository.findDistinctLeagueUidsWithAvailableFixtures())
            .thenReturn(listOf("league-1", "league-2"))
        whenever(availableFixtureJobReconciler.reconcileLeague("league-1"))
            .thenReturn(ReconcileResult.empty(fixtureUid = null, leagueUid = "league-1"))
        whenever(availableFixtureJobReconciler.reconcileLeague("league-2"))
            .thenReturn(ReconcileResult.empty(fixtureUid = null, leagueUid = "league-2"))

        val result = service.cleanupAndReconcile()

        assertThat(result.success).isTrue()
        assertThat(result.cleanupResult.deleted).isEqualTo(2)
        assertThat(result.reconcileResults).hasSize(2)

        inOrder(jobSchedulerService, fixtureCoreRepository, availableFixtureJobReconciler) {
            verify(jobSchedulerService).deleteStartupAvailableMatchJobs()
            verify(fixtureCoreRepository).findDistinctLeagueUidsWithAvailableFixtures()
            verify(availableFixtureJobReconciler).reconcileLeague("league-1")
            verify(availableFixtureJobReconciler).reconcileLeague("league-2")
        }
    }

    @Test
    fun `cleanup 또는 reconcile 실패가 있으면 startup cleanup 결과는 실패다`() {
        val service = service()
        whenever(jobSchedulerService.deleteStartupAvailableMatchJobs())
            .thenReturn(
                MatchJobCleanupResult(
                    deleted = 0,
                    skipped = 0,
                    errors =
                        listOf(
                            MatchJobCleanupError(
                                groupName = "pre-match",
                                jobKey = null,
                                operation = "list-jobs",
                                message = "quartz failed",
                            ),
                        ),
                ),
            )
        whenever(fixtureCoreRepository.findDistinctLeagueUidsWithAvailableFixtures()).thenReturn(listOf("league-1"))
        whenever(availableFixtureJobReconciler.reconcileLeague("league-1"))
            .thenReturn(
                ReconcileResult.empty(fixtureUid = null, leagueUid = "league-1")
                    .copy(
                        success = false,
                        errors =
                            listOf(
                                ReconcileError(
                                    fixtureUid = null,
                                    leagueUid = "league-1",
                                    phase = null,
                                    operation = "reconcile",
                                    message = "failed",
                                ),
                            ),
                    ),
            )

        val result = service.cleanupAndReconcile()

        assertThat(result.success).isFalse()
        assertThat(result.cleanupResult.success).isFalse()
        assertThat(result.reconcileResults.first().success).isFalse()
    }

    @Test
    fun `available fixture 리그 조회 실패는 앱 시작을 막지 않고 실패 결과로 남긴다`() {
        val service = service()
        whenever(jobSchedulerService.deleteStartupAvailableMatchJobs())
            .thenReturn(MatchJobCleanupResult(deleted = 1, skipped = 0, errors = emptyList()))
        whenever(fixtureCoreRepository.findDistinctLeagueUidsWithAvailableFixtures())
            .thenThrow(RuntimeException("db failed"))

        val result = service.cleanupAndReconcile()

        assertThat(result.success).isFalse()
        assertThat(result.cleanupResult.success).isTrue()
        assertThat(result.reconcileResults).hasSize(1)
        assertThat(result.reconcileResults.first().errors.first().operation)
            .isEqualTo("load-available-fixture-league-uids")
    }

    private fun service(): StartupMatchJobCleanup =
        StartupMatchJobCleanup(
            jobSchedulerService = jobSchedulerService,
            fixtureCoreRepository = fixtureCoreRepository,
            availableFixtureJobReconciler = availableFixtureJobReconciler,
        )
}
