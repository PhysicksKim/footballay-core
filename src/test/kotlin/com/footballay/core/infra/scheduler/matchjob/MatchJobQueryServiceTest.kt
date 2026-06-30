package com.footballay.core.infra.scheduler.matchjob

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.impl.matchers.GroupMatcher

@ExtendWith(MockitoExtension::class)
class MatchJobQueryServiceTest {
    @Mock
    private lateinit var scheduler: Scheduler

    @Test
    fun `리그 match job group의 job 목록을 조회한다`() {
        val service = MatchJobQueryService(scheduler)
        val jobs =
            setOf(
                JobKey.jobKey("available:pre:fixture-1", "league:match:league-1"),
                JobKey.jobKey("available:live:fixture-2", "league:match:league-1"),
            )
        whenever(scheduler.getJobKeys(any<GroupMatcher<JobKey>>())).thenReturn(jobs)

        val result = service.listLeagueMatchJobs("league-1")

        assertThat(result).isEqualTo(jobs)
    }

    @Test
    fun `job 존재 확인 중 Quartz 예외가 나면 false를 반환한다`() {
        val service = MatchJobQueryService(scheduler)
        val jobKey = JobKey.jobKey("available:pre:fixture-1", "league:match:league-1")
        whenever(scheduler.checkExists(jobKey)).thenThrow(RuntimeException("quartz failed"))

        val result = service.jobExists(jobKey)

        assertThat(result).isFalse()
    }
}
