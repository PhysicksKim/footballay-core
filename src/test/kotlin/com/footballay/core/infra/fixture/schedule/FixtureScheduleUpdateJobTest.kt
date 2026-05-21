package com.footballay.core.infra.fixture.schedule

import com.footballay.core.infra.scheduler.ReconcileError
import com.footballay.core.infra.scheduler.ReconcileResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.quartz.JobExecutionContext

@ExtendWith(MockitoExtension::class)
class FixtureScheduleUpdateJobTest {
    @Mock
    private lateinit var fixtureScheduleUpdater: FixtureScheduleUpdater

    @Mock
    private lateinit var context: JobExecutionContext

    @Test
    fun `execute는 available league fixture schedule batch sync를 실행한다`() {
        val job = FixtureScheduleUpdateJob(fixtureScheduleUpdater)
        whenever(fixtureScheduleUpdater.updateAvailableLeagues())
            .thenReturn(
                FixtureScheduleBatchUpdateResult(
                    targetLeagues = 1,
                    syncedLeagues = 1,
                    failedLeagues = 0,
                    skippedLeagues = 0,
                    syncedFixtures = 10,
                    reconcileFailures = emptyList(),
                ),
            )

        job.execute(context)

        verify(fixtureScheduleUpdater).updateAvailableLeagues()
    }

    @Test
    fun `batch 결과에 실패가 있어도 다음 cron 실행에서 복구할 수 있도록 예외를 던지지 않는다`() {
        val job = FixtureScheduleUpdateJob(fixtureScheduleUpdater)
        whenever(fixtureScheduleUpdater.updateAvailableLeagues())
            .thenReturn(
                FixtureScheduleBatchUpdateResult(
                    targetLeagues = 2,
                    syncedLeagues = 1,
                    failedLeagues = 1,
                    skippedLeagues = 0,
                    syncedFixtures = 10,
                    reconcileFailures =
                        listOf(
                            ReconcileResult.empty(
                                fixtureUid = null,
                                leagueUid = "league-1",
                            )
                                .copy(
                                    success = false,
                                    errors =
                                        listOf(
                                            ReconcileError(
                                                fixtureUid = null,
                                                leagueUid = "league-1",
                                                phase = null,
                                                operation = "register-or-replace",
                                                message = "quartz failed",
                                            ),
                                        ),
                                ),
                        ),
                ),
            )

        job.execute(context)

        verify(fixtureScheduleUpdater).updateAvailableLeagues()
    }
}
