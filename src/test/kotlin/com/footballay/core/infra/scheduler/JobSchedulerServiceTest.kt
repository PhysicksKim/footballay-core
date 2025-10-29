package com.footballay.core.infra.scheduler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.Trigger
import java.time.OffsetDateTime
import java.util.Date

/**
 * JobSchedulerService 단위 테스트
 *
 * Quartz Scheduler를 Mock으로 주입하여 Job 등록/삭제/조회 로직만 테스트합니다.
 * **실제 Job 실행은 테스트하지 않습니다** (경량화)
 */
@ExtendWith(MockitoExtension::class)
class JobSchedulerServiceTest {
    @Mock
    private lateinit var scheduler: Scheduler

    @InjectMocks
    private lateinit var jobSchedulerService: JobSchedulerService

    @Test
    fun `PreMatchJob 추가 성공`() {
        // Given
        val fixtureUid = "apisports:12345"
        val startTime = OffsetDateTime.now().plusHours(1)

        given(scheduler.checkExists(any(JobKey::class.java))).willReturn(false)
        given(scheduler.scheduleJob(any(JobDetail::class.java), any(Trigger::class.java))).willReturn(Date.from(startTime.toInstant()))

        // When
        val result = jobSchedulerService.addPreMatchJob(fixtureUid, startTime)

        // Then
        assertThat(result).isTrue()
        verify(scheduler).scheduleJob(any(JobDetail::class.java), any(Trigger::class.java))
    }

    @Test
    fun `이미 존재하는 PreMatchJob은 삭제 후 재등록`() {
        // Given
        val fixtureUid = "apisports:12345"
        val startTime = OffsetDateTime.now()

        given(scheduler.checkExists(any(JobKey::class.java))).willReturn(true)
        given(scheduler.deleteJob(any(JobKey::class.java))).willReturn(true)
        given(scheduler.scheduleJob(any(JobDetail::class.java), any(Trigger::class.java))).willReturn(Date.from(startTime.toInstant()))

        // When
        val result = jobSchedulerService.addPreMatchJob(fixtureUid, startTime)

        // Then
        assertThat(result).isTrue()
        verify(scheduler).deleteJob(any(JobKey::class.java))
        verify(scheduler).scheduleJob(any(JobDetail::class.java), any(Trigger::class.java))
    }

    @Test
    fun `LiveMatchJob 추가 성공`() {
        // Given
        val fixtureUid = "apisports:12345"
        val startTime = OffsetDateTime.now()

        given(scheduler.checkExists(any(JobKey::class.java))).willReturn(false)
        given(scheduler.scheduleJob(any(JobDetail::class.java), any(Trigger::class.java))).willReturn(Date.from(startTime.toInstant()))

        // When
        val result = jobSchedulerService.addLiveMatchJob(fixtureUid, startTime)

        // Then
        assertThat(result).isTrue()
        verify(scheduler).scheduleJob(any(JobDetail::class.java), any(Trigger::class.java))
    }

    @Test
    fun `PostMatchJob 추가 성공`() {
        // Given
        val fixtureUid = "apisports:12345"
        val startTime = OffsetDateTime.now()

        given(scheduler.checkExists(any(JobKey::class.java))).willReturn(false)
        given(scheduler.scheduleJob(any(JobDetail::class.java), any(Trigger::class.java))).willReturn(Date.from(startTime.toInstant()))

        // When
        val result = jobSchedulerService.addPostMatchJob(fixtureUid, startTime)

        // Then
        assertThat(result).isTrue()
        verify(scheduler).scheduleJob(any(JobDetail::class.java), any(Trigger::class.java))
    }

    @Test
    fun `Job 삭제 성공`() {
        // Given
        val jobKey = JobKey.jobKey("pre-match-apisports:12345", "pre-match")
        given(scheduler.deleteJob(jobKey)).willReturn(true)

        // When
        val result = jobSchedulerService.removeJob(jobKey)

        // Then
        assertThat(result).isTrue()
        verify(scheduler).deleteJob(jobKey)
    }

    @Test
    fun `Fixture의 모든 Job 삭제 (Pre+Live+Post)`() {
        // Given
        val fixtureUid = "apisports:12345"
        given(scheduler.deleteJob(any(JobKey::class.java))).willReturn(true)

        // When
        val deletedCount = jobSchedulerService.removeAllJobsForFixture(fixtureUid)

        // Then
        assertThat(deletedCount).isEqualTo(3)
        verify(scheduler, times(3)).deleteJob(any(JobKey::class.java))
    }

    @Test
    fun `Job 존재 여부 확인`() {
        // Given
        val jobKey = JobKey.jobKey("pre-match-apisports:12345", "pre-match")
        given(scheduler.checkExists(jobKey)).willReturn(true)

        // When
        val exists = jobSchedulerService.jobExists(jobKey)

        // Then
        assertThat(exists).isTrue()
        verify(scheduler).checkExists(jobKey)
    }

    @Test
    fun `Scheduler 예외 발생 시 false 반환 - PreMatchJob 추가`() {
        // Given
        val fixtureUid = "apisports:12345"
        given(scheduler.checkExists(any(JobKey::class.java))).willThrow(RuntimeException("Scheduler error"))

        // When
        val result = jobSchedulerService.addPreMatchJob(fixtureUid)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `존재하지 않는 Job 삭제 시 false 반환`() {
        // Given
        val jobKey = JobKey.jobKey("pre-match-apisports:99999", "pre-match")
        given(scheduler.deleteJob(jobKey)).willReturn(false)

        // When
        val result = jobSchedulerService.removeJob(jobKey)

        // Then
        assertThat(result).isFalse()
        verify(scheduler).deleteJob(jobKey)
    }
}
