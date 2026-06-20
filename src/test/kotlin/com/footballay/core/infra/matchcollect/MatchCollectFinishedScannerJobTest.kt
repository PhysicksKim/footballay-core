package com.footballay.core.infra.matchcollect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@ExtendWith(MockitoExtension::class)
class MatchCollectFinishedScannerJobTest {
    @Mock
    private lateinit var manager: LeagueMatchCollectManager

    @Mock
    private lateinit var context: JobExecutionContext

    @Test
    fun `scanner job은 동시 실행을 허용하지 않는다`() {
        assertThat(MatchCollectFinishedScannerJob::class.java.getAnnotation(DisallowConcurrentExecution::class.java)).isNotNull()
    }

    @Test
    fun `execute는 due FINISHED fixture collect batch를 실행한다`() {
        val job = MatchCollectFinishedScannerJob(manager)
        whenever(manager.collectDueFinishedFixtures())
            .thenReturn(
                FinishedMatchCollectBatchResult(
                    candidates = 1,
                    due = 1,
                    collected = 1,
                    skipped = 0,
                    failed = 0,
                    results = emptyList(),
                ),
            )

        job.execute(context)

        verify(manager).collectDueFinishedFixtures()
    }

    @Test
    fun `batch 실패가 있어도 다음 cron 실행에서 재시도할 수 있도록 예외를 던지지 않는다`() {
        val job = MatchCollectFinishedScannerJob(manager)
        whenever(manager.collectDueFinishedFixtures())
            .thenReturn(
                FinishedMatchCollectBatchResult(
                    candidates = 1,
                    due = 1,
                    collected = 0,
                    skipped = 0,
                    failed = 1,
                    results = listOf(MatchCollectExecutionResult.Failed("fixture-1", "provider failed")),
                ),
            )

        job.execute(context)

        verify(manager).collectDueFinishedFixtures()
    }
}
